package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.onyx.android.sdk.api.device.screensaver.ScreenResourceManager
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "SaveWallpaper"
private const val SCREEN_TO_GRID_RATIO = 10

fun Resolution.toRowsCols(): Pair<Int, Int> {
    val rows = width / SCREEN_TO_GRID_RATIO
    val cols = height / (SCREEN_TO_GRID_RATIO * ratio).toInt()
    return rows to cols
}

suspend fun saveWallpaper(
    context: Context,
    dataStore: UserDataStore,
    showToast: Boolean,
    updateGame: Boolean = true,
) {
    val resolution = getScreenResolution(context)
    Log.d(TAG, "resolution: $resolution")
    val fgColor: Int = (dataStore[UserDataStore.Keys.FG_COLOR].first() ?: RANDOM_COLOR)
        .let { color ->
            if (color == RANDOM_COLOR) {
                Colors.ALL.chooseRandom(except = setOf(Color.White.toArgb())).value.toArgb()
            } else {
                color
            }
        }
    val bgColor: Int = dataStore[UserDataStore.Keys.BG_COLOR].first() ?: Color.White.toArgb()
        .let { color ->
            if (color == RANDOM_COLOR) {
                Colors.ALL.chooseRandom(
                    except = setOf(fgColor, Color.Black.toArgb()),
                ).value.toArgb()
            } else {
                color
            }
        }

    val (rows, cols) = resolution.toRowsCols()

    val gridController = dataStore[UserDataStore.Keys.GAME_STATE].first().let {
        GolController(rows, cols, it)
    }

    if (updateGame) {
        gridController.update()
        dataStore[UserDataStore.Keys.GAME_STATE] = gridController.toString()
    }

    val bitmap = createBitmapFromBooleanArray(
        width = resolution.width,
        height = resolution.height,
        trueColor = fgColor,
        falseColor = bgColor,
        grid = gridController.grid,
    )

    withContext(Dispatchers.IO) {
        val path = saveBitmapToPictures(
            context = context,
            bitmap = bitmap,
            fileName = "screenshot_${System.currentTimeMillis()}.png",
        )
        Log.d(TAG, "saved bitmap: $path")
        val isSuccess = ScreenResourceManager.setScreensaver(
            context,
            path,
            showToast,
        )
        Log.d(TAG, "setScreensaver: $isSuccess")
    }
}
