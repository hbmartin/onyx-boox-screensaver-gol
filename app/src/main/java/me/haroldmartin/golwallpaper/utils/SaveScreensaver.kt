package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.haroldmartin.golwallpaper.data.CalendarPreferences
import me.haroldmartin.golwallpaper.data.LayersSerializer
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.data.UserDataStore.Keys
import me.haroldmartin.golwallpaper.domain.CalendarAgendaResult
import me.haroldmartin.golwallpaper.domain.DEFAULT_BG
import me.haroldmartin.golwallpaper.domain.DEFAULT_CELL_SIZE
import me.haroldmartin.golwallpaper.domain.DEFAULT_WRAP_EDGES
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.LoadCalendarAgenda
import me.haroldmartin.golwallpaper.domain.OutputOrientation
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
import me.haroldmartin.golwallpaper.ui.theme.ALL_COLORS
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "SaveWallpaper"
private const val ONYX_SCREENSAVER_TYPE = 16

class SaveScreensaver(
    val dataStore: UserDataStore,
    private val calendarPreferences: CalendarPreferences,
    private val loadCalendarAgenda: LoadCalendarAgenda,
    val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(context: Context, showHint: Boolean, step: Boolean = true) {
        withContext(ioDispatcher) {
            val outputOrientation = OutputOrientation.fromString(
                dataStore[Keys.OUTPUT_ORIENTATION].first(),
            )
            val resolution = getScreenResolution(context).oriented(outputOrientation)
            val bgColor = getBgColor()
            val cellSize = (dataStore[Keys.CELL_SIZE].first() ?: DEFAULT_CELL_SIZE).coerceAtLeast(1)
            val (rows, cols) = resolution.toRowsCols(cellSize)
            val areEdgesWrapped = dataStore[Keys.WRAP_EDGES].first() ?: DEFAULT_WRAP_EDGES
            val processed = advanceLayers(
                rows = rows,
                cols = cols,
                step = step,
                bgColor = bgColor,
                areEdgesWrapped = areEdgesWrapped,
            )
            val updatedLayers = processed.map(ProcessedLayer::layer)

            val calendarSettings = calendarPreferences.settings.first()
            val calendarResult = loadCalendarAgenda(calendarSettings)
            val stats = if (dataStore[Keys.SHOW_STATS].first() == true) {
                RenderStats(
                    generation = updatedLayers.filter(Layer::isEnabled).maxOfOrNull(Layer::generation) ?: 0,
                    population = processed.sumOf(ProcessedLayer::population),
                )
            } else {
                null
            }
            val bitmap = Bitmap.createBitmap(
                resolution.width,
                resolution.height,
                Bitmap.Config.ARGB_8888,
            )
            renderDeviceBitmap(
                context = context,
                bitmap = bitmap,
                backgroundColor = bgColor,
                layers = processed.mapNotNull(ProcessedLayer::renderLayer),
                overlays = DeviceOverlays(
                    calendarAgenda = (calendarResult as? CalendarAgendaResult.Available)?.agenda,
                    calendarSettings = calendarSettings,
                    stats = stats,
                ),
            )

            dispatchBitmap(context = context, bitmap = bitmap, showHint = showHint)
            bitmap.recycle()
            Log.d(TAG, "wallpaper updated, ${getAppMemoryUsage(context)}")
        }
    }

    // Decode, advance, and persist the layers in a single atomic update so overlapping
    // saves (worker + UI) cannot lose each other's generation increments.
    @Suppress("AvoidVarsExceptWithDelegate")
    private suspend fun advanceLayers(
        rows: Int,
        cols: Int,
        step: Boolean,
        bgColor: Int,
        areEdgesWrapped: Boolean,
    ): List<ProcessedLayer> {
        var processed = emptyList<ProcessedLayer>()
        dataStore.update(Keys.LAYERS) { storedLayers ->
            processed = LayersSerializer.decode(storedLayers).map { layer ->
                processLayer(
                    layer = layer,
                    rows = rows,
                    cols = cols,
                    step = step,
                    bgColor = bgColor,
                    areEdgesWrapped = areEdgesWrapped,
                )
            }
            LayersSerializer.encode(processed.map(ProcessedLayer::layer))
        }
        return processed
    }

    @Suppress("LongParameterList")
    private fun processLayer(
        layer: Layer,
        rows: Int,
        cols: Int,
        step: Boolean,
        bgColor: Int,
        areEdgesWrapped: Boolean,
    ): ProcessedLayer {
        if (!layer.isEnabled) return ProcessedLayer(layer = layer)

        val controller = newController(
            rows = rows,
            cols = cols,
            state = layer.state,
            rule = layer.rule,
            areEdgesWrapped = areEdgesWrapped,
        )
        if (step) controller.update()
        val updatedLayer = layer.copy(
            state = controller.toString(),
            generation = layer.generation + if (step) 1 else 0,
        )
        return ProcessedLayer(
            layer = updatedLayer,
            renderLayer = RenderLayer(
                grid = controller.grid,
                fgColor = resolveColor(layer.fgColor, except = setOf(bgColor)),
            ),
            population = controller.population,
        )
    }

    private fun newController(
        rows: Int,
        cols: Int,
        state: String?,
        rule: String,
        areEdgesWrapped: Boolean,
    ): GolController =
        try {
            if (state == null) {
                GolController(
                    rows = rows,
                    columns = cols,
                    initialPattern = ".",
                    rule = rule,
                    areEdgesWrapped = areEdgesWrapped,
                )
                    .apply { reset(pattern = null) }
            } else {
                GolController(
                    rows = rows,
                    columns = cols,
                    initialPattern = state,
                    rule = rule,
                    areEdgesWrapped = areEdgesWrapped,
                )
            }
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Pattern does not fit ${rows}x$cols grid, resetting to random", exception)
            GolController(
                rows = rows,
                columns = cols,
                initialPattern = ".",
                rule = rule,
                areEdgesWrapped = areEdgesWrapped,
            )
                .apply { reset(pattern = null) }
        }

    private suspend fun dispatchBitmap(context: Context, bitmap: Bitmap, showHint: Boolean) {
        if (isOnyxDevice()) {
            setOnyxScreensaver(context, bitmap, showHint)
        } else {
            val target = WallpaperTarget.fromString(dataStore[Keys.WALLPAPER_TARGET].first())
            if (!setDeviceWallpaper(context, bitmap, target)) {
                Log.e(TAG, "Failed to set device wallpaper")
            }
        }
    }

    private fun setOnyxScreensaver(context: Context, bitmap: Bitmap, showHint: Boolean) {
        val uriAndFakePath = saveBitmapToPictures(
            context = context,
            bitmap = bitmap,
            fileName = "$SCREENSHOT_FILE_PREFIX${System.currentTimeMillis()}.png",
        )

        if (uriAndFakePath == null) {
            Log.e(TAG, "failed to save image")
            return
        }

        Log.d(TAG, "saved bitmap, ${getAppMemoryUsage(context)}")
        deleteOrphanedScreenshots(context, keep = uriAndFakePath.first)
        context.setScreensaver(uriAndFakePath.second, showHint)
    }

    private suspend fun getBgColor(): Int = resolveColor(
        color = dataStore[Keys.BG_COLOR].first() ?: DEFAULT_BG,
        except = setOf(Color.Black.toArgb()),
    )

    private fun resolveColor(color: Int, except: Set<Int>): Int = if (color == RANDOM_COLOR) {
        ALL_COLORS.chooseRandom(except).color.toArgb()
    } else {
        color
    }
}

private data class ProcessedLayer(
    val layer: Layer,
    val renderLayer: RenderLayer? = null,
    val population: Int = 0,
)

private fun Context.setScreensaver(filePath: String, showHint: Boolean) {
    sendBroadcast(
        Intent("onyx.action.SCREENSAVER")
            .putExtra("type", ONYX_SCREENSAVER_TYPE)
            .putExtra("file", filePath)
            .putExtra("show_result_hint", showHint),
    )
}
