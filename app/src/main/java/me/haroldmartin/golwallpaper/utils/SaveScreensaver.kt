package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.data.UserDataStore.Keys
import me.haroldmartin.golwallpaper.domain.DEFAULT_CELL_SIZE
import me.haroldmartin.golwallpaper.domain.DEFAULT_RULE
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
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
            val cellSize = dataStore[Keys.CELL_SIZE].first() ?: DEFAULT_CELL_SIZE
            val rule = dataStore[Keys.RULE].first() ?: DEFAULT_RULE

            val (rows, cols) = resolution.toRowsCols(cellSize)

            val gridController = pattern?.let {
                GolController(rows = rows, columns = cols, initialPattern = it, rule = rule)
            } ?: GolController(
                rows = rows,
                columns = cols,
                initialPattern = dataStore[Keys.GAME_STATE].first(),
                rule = rule,
            ).apply { update() }

            val generation = if (pattern != null) {
                0
            } else {
                (dataStore[Keys.GENERATION].first() ?: 0) + 1
            }
            dataStore[Keys.GENERATION] = generation
            dataStore[Keys.GAME_STATE] = gridController.toString()

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

    private suspend fun setOnyxScreensaver(context: Context, bitmap: Bitmap, showHint: Boolean) {
        val uriAndFakePath = saveBitmapToPictures(
            context = context,
            bitmap = bitmap,
            fileName = "screenshot_${System.currentTimeMillis()}.png",
        )

        if (uriAndFakePath == null) {
            Log.e(TAG, "failed to save image")
            return
        }

        Log.d(TAG, "saved bitmap, ${getAppMemoryUsage(context)}")
        dataStore[Keys.PREV_IMAGE_URI].first()?.let {
            deleteImage(context, it.toUri())
        }
        dataStore[Keys.PREV_IMAGE_URI] = uriAndFakePath.first.toString()

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
