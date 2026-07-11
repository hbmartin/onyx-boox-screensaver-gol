package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.data.UserDataStore.Keys
import me.haroldmartin.golwallpaper.domain.DEFAULT_AUTO_RESEED
import me.haroldmartin.golwallpaper.domain.DEFAULT_CELL_SIZE
import me.haroldmartin.golwallpaper.domain.DEFAULT_RULE
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
import me.haroldmartin.golwallpaper.domain.shouldReseed
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "SaveWallpaper"
private const val ONYX_SCREENSAVER_TYPE = 16

class SaveScreensaver(val dataStore: UserDataStore, val ioDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(context: Context, showHint: Boolean, pattern: String? = null) {
        withContext(ioDispatcher) {
            val resolution = getScreenResolution(context)
            val fgColor = getFgColor()
            val bgColor: Int = getBgColor(fgColor)
            val cellSize =
                (dataStore[Keys.CELL_SIZE].first() ?: DEFAULT_CELL_SIZE).coerceAtLeast(1)
            val rule = dataStore[Keys.RULE].first() ?: DEFAULT_RULE

            val (rows, cols) = resolution.toRowsCols(cellSize)

            val (gridController, generation) = buildController(
                rows = rows,
                cols = cols,
                rule = rule,
                pattern = pattern,
            )
            dataStore[Keys.GENERATION] = generation

            val bitmap = createBitmapFromBooleanArray(
                width = resolution.width,
                height = resolution.height,
                trueColor = fgColor,
                falseColor = bgColor,
                grid = gridController.grid,
            )

            if (dataStore[Keys.SHOW_STATS].first() == true) {
                drawStatsOverlay(
                    bitmap = bitmap,
                    text = context.getString(
                        R.string.stats_overlay,
                        generation,
                        gridController.population,
                    ),
                    textColor = fgColor,
                    backgroundColor = bgColor,
                )
            }

            if (isOnyxDevice()) {
                setOnyxScreensaver(context, bitmap, showHint)
            } else {
                val target = WallpaperTarget.fromString(dataStore[Keys.WALLPAPER_TARGET].first())
                setDeviceWallpaper(context, bitmap, target)
            }

            bitmap.recycle()
            Log.d(TAG, "wallpaper updated, ${getAppMemoryUsage(context)}")
        }
    }

    // Produces the controller to render this frame and the generation counter to display.
    // An explicit `pattern` starts a fresh game at generation 0; otherwise the stored game
    // state is advanced one step, reseeding first if the board has died or stagnated.
    private suspend fun buildController(
        rows: Int,
        cols: Int,
        rule: String,
        pattern: String?,
    ): Pair<GolController, Int> =
        if (pattern != null) {
            val controller = newController(rows = rows, cols = cols, state = pattern, rule = rule)
            val state = controller.toString()
            dataStore[Keys.GAME_STATE] = state
            dataStore[Keys.GAME_STATE_PREV] = state
            controller to 0
        } else {
            advanceController(rows = rows, cols = cols, rule = rule)
        }

    // Advances the stored game one generation. When auto-reseed is enabled and the resulting
    // board is dead or stagnant (equal to either of the last two generations), the board is
    // reseeded with random noise and the generation counter restarts at 0. GAME_STATE holds
    // the latest generation and GAME_STATE_PREV the one before it, so period-2 oscillators
    // (which match the generation two steps back) are detected.
    private suspend fun advanceController(
        rows: Int,
        cols: Int,
        rule: String,
    ): Pair<GolController, Int> {
        val prevState = dataStore[Keys.GAME_STATE].first()
        val prevPrevState = dataStore[Keys.GAME_STATE_PREV].first()
        val controller = newController(rows = rows, cols = cols, state = prevState, rule = rule)
            .apply { update() }
        val newState = controller.toString()
        val autoReseed = dataStore[Keys.AUTO_RESEED].first() ?: DEFAULT_AUTO_RESEED

        return if (autoReseed &&
            shouldReseed(controller.population, newState, prevState, prevPrevState)
        ) {
            controller.reset(pattern = null)
            val reseeded = controller.toString()
            dataStore[Keys.GAME_STATE] = reseeded
            dataStore[Keys.GAME_STATE_PREV] = reseeded
            controller to 0
        } else {
            dataStore[Keys.GAME_STATE] = newState
            dataStore[Keys.GAME_STATE_PREV] = prevState ?: newState
            controller to ((dataStore[Keys.GENERATION].first() ?: 0) + 1)
        }
    }

    // The stored game state (or a chosen pattern) can be too large for the grid after the
    // user shrinks it by picking a bigger cell size — fall back to a random grid instead
    // of crashing the update.
    private fun newController(rows: Int, cols: Int, state: String?, rule: String): GolController =
        try {
            GolController(rows = rows, columns = cols, initialPattern = state, rule = rule)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Pattern does not fit ${rows}x$cols grid, resetting to random", e)
            GolController(rows = rows, columns = cols, initialPattern = ".", rule = rule)
                .apply { reset(pattern = null) }
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
        // Sweep every leftover screenshot except the one we just saved, so orphans from a
        // killed process or a failed delete never accumulate in the user's Downloads folder.
        deleteOrphanedScreenshots(context, keep = uriAndFakePath.first)

        context.setScreensaver(
            uriAndFakePath.second,
            showHint,
        )
    }

    private suspend fun getFgColor(): Int =
        (dataStore[Keys.FG_COLOR].first() ?: Color.Black.toArgb())
            .let { color ->
                if (color == RANDOM_COLOR) {
                    Colors.ALL.chooseRandom(setOf(Color.White.toArgb())).value.toArgb()
                } else {
                    color
                }
            }

    private suspend fun getBgColor(fgColor: Int): Int =
        (dataStore[Keys.BG_COLOR].first() ?: Color.White.toArgb())
            .let { color ->
                if (color == RANDOM_COLOR) {
                    Colors.ALL.chooseRandom(setOf(fgColor, Color.Black.toArgb())).value.toArgb()
                } else {
                    color
                }
            }
}

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
