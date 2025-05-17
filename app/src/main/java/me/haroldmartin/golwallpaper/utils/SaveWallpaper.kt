package me.haroldmartin.golwallpaper.utils

import GolController
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.onyx.android.sdk.api.device.screensaver.ScreenResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.haroldmartin.golwallpaper.data.UserDataStore

private const val TAG = "SaveWallpaper"
private const val SCREEN_TO_GRID_RATIO = 10

suspend fun saveWallpaper(context: Context, repository: UserDataStore, showToast: Boolean) {
    val resolution = getScreenResolution(context)
    Log.d(TAG, "resolution: $resolution")
    val fgColor = repository[UserDataStore.Keys.FG_COLOR].first() ?: Color.Black.toArgb()
    val bgColor = repository[UserDataStore.Keys.BG_COLOR].first() ?: Color.White.toArgb()
    val rows = resolution.width / SCREEN_TO_GRID_RATIO
    val cols = resolution.height / (SCREEN_TO_GRID_RATIO * resolution.ratio).toInt()
    Log.d(TAG, "rows: $rows, cols: $cols")
    // TODO: test this on fresh install
    val gridController = repository[UserDataStore.Keys.GAME_STATE].first().let {
        GolController(rows, cols, it)
    }
    gridController.update()
    repository[UserDataStore.Keys.GAME_STATE] = gridController.toString()
    val bitmap = createBitmapFromBooleanArray(
        width = resolution.width,
        height = resolution.height,
        trueColor = fgColor,
        falseColor = bgColor,
        grid = gridController.grid
    )

    withContext(Dispatchers.IO) {
        val path = saveBitmapToPictures(
            context = context,
            bitmap = bitmap,
            fileName = "screenshot_${System.currentTimeMillis()}.png"
        )
        ScreenResourceManager.setScreensaver(
            context,
            path,
            showToast
        )
    }
}