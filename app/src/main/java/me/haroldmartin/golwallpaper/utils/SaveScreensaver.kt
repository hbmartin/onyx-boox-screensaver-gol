package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import com.onyx.android.sdk.api.device.screensaver.ScreenResourceManager
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.data.UserDataStore.Keys
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "SaveWallpaper"
private const val SCREEN_TO_GRID_RATIO = 10

class SaveScreensaver(val dataStore: UserDataStore, val ioDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(context: Context, showToast: Boolean, pattern: String? = null) {
        withContext(ioDispatcher) {
            val resolution = getScreenResolution(context)
            val fgColor = getFgColor()
            val bgColor: Int = getBgColor(fgColor)

            val (rows, cols) = resolution.toRowsCols()

            val gridController = pattern?.let { GolController(rows, cols, it) }
                ?: GolController(rows, cols, dataStore[Keys.GAME_STATE].first())
                    .apply { update() }

            dataStore[Keys.GAME_STATE] = gridController.toString()

            val bitmap = createBitmapFromBooleanArray(
                width = resolution.width,
                height = resolution.height,
                trueColor = fgColor,
                falseColor = bgColor,
                grid = gridController.grid,
            )

            val uriAndFakePath = saveBitmapToPictures(
                context = context,
                bitmap = bitmap,
                fileName = "screenshot_${System.currentTimeMillis()}.png",
            )

            if (uriAndFakePath == null) {
                Log.e(TAG, "failed to save image")
                return@withContext
            }

            Log.d(TAG, "saved bitmap, ${getAppMemoryUsage(context)}")
            dataStore[Keys.PREV_IMAGE_URI].first()?.let {
                deleteImage(context, it.toUri())
            }
            dataStore[Keys.PREV_IMAGE_URI] = uriAndFakePath.first.toString()

            bitmap.recycle()

            val isSuccess = ScreenResourceManager.setScreensaver(
                context,
                uriAndFakePath.second,
                showToast,
            )
            Log.d(TAG, "setScreensaver: $isSuccess , ${getAppMemoryUsage(context)}")
        }
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

private fun Resolution.toRowsCols(): Pair<Int, Int> {
    val rows = width / SCREEN_TO_GRID_RATIO
    val cols = height / (SCREEN_TO_GRID_RATIO * ratio).toInt()
    return rows to cols
}
