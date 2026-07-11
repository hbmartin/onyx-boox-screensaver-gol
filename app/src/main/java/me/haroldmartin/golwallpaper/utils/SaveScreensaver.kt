package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.data.LayersSerializer
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.data.UserDataStore.Keys
import me.haroldmartin.golwallpaper.domain.DEFAULT_BG
import me.haroldmartin.golwallpaper.domain.DEFAULT_CELL_SIZE
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "SaveWallpaper"
private const val ONYX_SCREENSAVER_TYPE = 16
private const val OPAQUE_ALPHA = 0xFF000000.toInt()
private const val RGB_MASK = 0x00FFFFFF

class SaveScreensaver(val dataStore: UserDataStore, val ioDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(context: Context, showHint: Boolean, step: Boolean = true) {
        withContext(ioDispatcher) {
            val resolution = getScreenResolution(context)
            val bgColor = getBgColor()
            val cellSize = (dataStore[Keys.CELL_SIZE].first() ?: DEFAULT_CELL_SIZE).coerceAtLeast(1)
            val (rows, cols) = resolution.toRowsCols(cellSize)
            val processed = advanceLayers(rows = rows, cols = cols, step = step, bgColor = bgColor)
            val updatedLayers = processed.map(ProcessedLayer::layer)

            val bitmap = createCompositeBitmap(
                width = resolution.width,
                height = resolution.height,
                backgroundColor = bgColor,
                layers = processed.mapNotNull(ProcessedLayer::renderLayer),
            )

            if (dataStore[Keys.SHOW_STATS].first() == true) {
                drawStatsOverlay(
                    bitmap = bitmap,
                    text = context.getString(
                        R.string.stats_overlay,
                        updatedLayers.filter(Layer::isEnabled).maxOfOrNull(Layer::generation) ?: 0,
                        processed.sumOf(ProcessedLayer::population),
                    ),
                    textColor = bgColor.inverseRgb(),
                    backgroundColor = bgColor,
                )
            }

            dispatchBitmap(context = context, bitmap = bitmap, showHint = showHint)
            bitmap.recycle()
            Log.d(TAG, "wallpaper updated, ${getAppMemoryUsage(context)}")
        }
    }

    // Decode, advance, and persist the layers in a single atomic update so overlapping
    // saves (worker + UI) cannot lose each other's generation increments.
    @Suppress("AvoidVarsExceptWithDelegate")
    private suspend fun advanceLayers(rows: Int, cols: Int, step: Boolean, bgColor: Int): List<ProcessedLayer> {
        var processed = emptyList<ProcessedLayer>()
        dataStore.update(Keys.LAYERS) { storedLayers ->
            processed = LayersSerializer.decode(storedLayers).map { layer ->
                processLayer(layer = layer, rows = rows, cols = cols, step = step, bgColor = bgColor)
            }
            LayersSerializer.encode(processed.map(ProcessedLayer::layer))
        }
        return processed
    }

    private fun processLayer(
        layer: Layer,
        rows: Int,
        cols: Int,
        step: Boolean,
        bgColor: Int,
    ): ProcessedLayer {
        if (!layer.isEnabled) return ProcessedLayer(layer = layer)

        val controller = newController(rows = rows, cols = cols, state = layer.state, rule = layer.rule)
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

    private fun newController(rows: Int, cols: Int, state: String?, rule: String): GolController =
        try {
            if (state == null) {
                GolController(rows = rows, columns = cols, initialPattern = ".", rule = rule)
                    .apply { reset(pattern = null) }
            } else {
                GolController(rows = rows, columns = cols, initialPattern = state, rule = rule)
            }
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Pattern does not fit ${rows}x$cols grid, resetting to random", exception)
            GolController(rows = rows, columns = cols, initialPattern = ".", rule = rule)
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

    private suspend fun setOnyxScreensaver(context: Context, bitmap: Bitmap, showHint: Boolean) {
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
        Colors.ALL.chooseRandom(except).value.toArgb()
    } else {
        color
    }
}

private data class ProcessedLayer(
    val layer: Layer,
    val renderLayer: RenderLayer? = null,
    val population: Int = 0,
)

private fun Int.inverseRgb(): Int = (this xor RGB_MASK) or OPAQUE_ALPHA

private fun Resolution.toRowsCols(cellSize: Int): Pair<Int, Int> {
    val rows = width / cellSize
    val cols = height / (cellSize * ratio).toInt()
    return rows to cols
}

private fun Context.setScreensaver(filePath: String, showHint: Boolean) {
    sendBroadcast(
        Intent("onyx.action.SCREENSAVER")
            .putExtra("type", ONYX_SCREENSAVER_TYPE)
            .putExtra("file", filePath)
            .putExtra("show_result_hint", showHint),
    )
}
