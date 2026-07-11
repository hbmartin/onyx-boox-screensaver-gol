package me.haroldmartin.golwallpaper.domain

interface SaveSettings {
    suspend fun setCellSize(size: Int)
    suspend fun setUpdateIntervalMins(mins: Long)
    suspend fun setRule(rule: String)
    suspend fun setShowStats(show: Boolean)
    suspend fun setWallpaperTarget(target: WallpaperTarget)
    suspend fun setBatteryThreshold(pct: Int)
}
