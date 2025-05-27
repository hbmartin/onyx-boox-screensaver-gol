package me.haroldmartin.golwallpaper

import android.service.wallpaper.WallpaperService

class MyWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return GolEngine()
    }
}
