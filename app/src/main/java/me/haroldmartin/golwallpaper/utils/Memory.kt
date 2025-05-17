package me.haroldmartin.golwallpaper.utils

import android.app.ActivityManager
import android.content.Context

private const val BYTES_PER_MEGABYTE = 1_048_576

fun getAppMemoryUsage(context: Context): String {
    // Get memory info using ActivityManager
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memoryInfo)

    // Get total memory allocated to the app (Java heap)
    val runtime = Runtime.getRuntime()
    val totalMemory = runtime.totalMemory() / BYTES_PER_MEGABYTE
    val freeMemory = runtime.freeMemory() / BYTES_PER_MEGABYTE
    val usedMemory = totalMemory - freeMemory

    // Return a formatted string with memory information
    return "App Memory Usage: " +
        "Total Memory: $totalMemory MB, " +
        "Used Memory: $usedMemory MB, " +
        "Free Memory: $freeMemory MB"
}
