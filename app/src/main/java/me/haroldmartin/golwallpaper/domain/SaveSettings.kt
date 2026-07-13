package me.haroldmartin.golwallpaper.domain

interface SaveSettings {
    suspend fun setCellSize(size: Int)
    suspend fun setUpdateIntervalMins(mins: Long)
    suspend fun setShowStats(show: Boolean)
    suspend fun setWallpaperTarget(target: WallpaperTarget)
    suspend fun setBatteryThreshold(pct: Int)
    suspend fun setWrapEdges(wrap: Boolean)
    suspend fun setOutputOrientation(orientation: OutputOrientation)
}
