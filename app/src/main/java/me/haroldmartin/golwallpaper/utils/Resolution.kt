package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import kotlin.math.roundToInt

typealias Resolution = Pair<Int, Int>

val Resolution.width: Int get() = first
val Resolution.height: Int get() = second

fun Resolution.toRowsCols(cellSize: Int): Pair<Int, Int> {
    require(cellSize > 0) { "Cell size must be positive" }
    val rows = (height / cellSize).coerceAtLeast(1)
    val cols = (width / cellSize).coerceAtLeast(1)
    return rows to cols
}

fun Resolution.scaledToFit(maxWidth: Int, maxHeight: Int): Resolution {
    require(width > 0 && height > 0) { "Resolution must be positive" }
    require(maxWidth > 0 && maxHeight > 0) { "Bounds must be positive" }
    val scale = minOf(
        maxWidth.toDouble() / width,
        maxHeight.toDouble() / height,
        1.0,
    )
    return Resolution(
        (width * scale).roundToInt().coerceAtLeast(1),
        (height * scale).roundToInt().coerceAtLeast(1),
    )
}

@Suppress("Deprecation")
fun getScreenResolution(context: Context): Resolution {
    val displayMetrics = DisplayMetrics()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        // For Android 10 (API 29) and below
        windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    } else {
        // For Android 11 (API 30) and above
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        display?.getRealMetrics(displayMetrics)
    }

    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels

    return Resolution(width, height)
}
