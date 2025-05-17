package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

typealias Resolution = Pair<Int, Int>

val Resolution.width: Int get() = first
val Resolution.height: Int get() = second
val Resolution.ratio: Float
    get() = if (width > height) {
        width.toFloat() / height
    } else {
        height.toFloat() / width
    }

fun getScreenResolution(context: Context): Resolution {
    val displayMetrics = DisplayMetrics()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // For Android 10 (API 29) and below
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }
    // For Android 11 (API 30) and above
    else {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        display?.getRealMetrics(displayMetrics)
    }

    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels

    return Pair(width, height)
}
