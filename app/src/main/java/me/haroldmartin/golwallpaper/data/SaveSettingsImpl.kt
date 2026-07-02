package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.SaveSettings
import me.haroldmartin.golwallpaper.domain.WallpaperTarget

class SaveSettingsImpl(val dataStore: UserDataStore) : SaveSettings {
    override suspend fun setCellSize(size: Int) {
        dataStore[UserDataStore.Keys.CELL_SIZE] = size
    }

    override suspend fun setUpdateIntervalMins(mins: Long) {
        dataStore[UserDataStore.Keys.UPDATE_INTERVAL_MINS] = mins
    }

    override suspend fun setRule(rule: String) {
        dataStore[UserDataStore.Keys.RULE] = rule
    }

    override suspend fun setShowStats(show: Boolean) {
        dataStore[UserDataStore.Keys.SHOW_STATS] = show
    }

    override suspend fun setWallpaperTarget(target: WallpaperTarget) {
        dataStore[UserDataStore.Keys.WALLPAPER_TARGET] = target.name
    }
}
