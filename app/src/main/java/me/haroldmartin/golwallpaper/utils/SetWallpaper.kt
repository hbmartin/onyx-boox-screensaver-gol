package me.haroldmartin.golwallpaper.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
import java.io.IOException

private const val TAG = "SetWallpaper"
private const val ONYX = "onyx"

fun isOnyxDevice(): Boolean = Build.MANUFACTURER.contains(ONYX, ignoreCase = true) ||
    Build.BRAND.contains(ONYX, ignoreCase = true)

fun setDeviceWallpaper(context: Context, bitmap: Bitmap, target: WallpaperTarget): Boolean {
    val flags = when (target) {
        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
        WallpaperTarget.BOTH -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
    }
    return try {
        WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, flags)
        true
    } catch (e: IOException) {
        Log.e(TAG, "Failed to set wallpaper", e)
        false
    }
}
