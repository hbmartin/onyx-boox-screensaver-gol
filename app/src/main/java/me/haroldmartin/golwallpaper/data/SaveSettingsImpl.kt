package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.SaveSettings
import me.haroldmartin.golwallpaper.domain.WallpaperTarget

private val RULE_REGEX = Regex("""B\d*/S\d*""", RegexOption.IGNORE_CASE)
private const val PERCENT_MAX = 100

class SaveSettingsImpl(val dataStore: UserDataStore) : SaveSettings {
    override suspend fun setCellSize(size: Int) {
        require(size > 0) { "Cell size must be positive" }
        dataStore[UserDataStore.Keys.CELL_SIZE] = size
    }

    override suspend fun setUpdateIntervalMins(mins: Long) {
        require(mins > 0) { "Update interval must be positive" }
        dataStore[UserDataStore.Keys.UPDATE_INTERVAL_MINS] = mins
    }

    override suspend fun setRule(rule: String) {
        require(RULE_REGEX.matches(rule)) { "Rule must match B<digits>/S<digits>: $rule" }
        dataStore[UserDataStore.Keys.RULE] = rule
    }

    override suspend fun setShowStats(show: Boolean) {
        dataStore[UserDataStore.Keys.SHOW_STATS] = show
    }

    override suspend fun setWallpaperTarget(target: WallpaperTarget) {
        dataStore[UserDataStore.Keys.WALLPAPER_TARGET] = target.name
    }

    override suspend fun setBatteryThreshold(pct: Int) {
        require(pct in 0..PERCENT_MAX) { "Battery threshold must be 0..$PERCENT_MAX: $pct" }
        dataStore[UserDataStore.Keys.BATTERY_THRESHOLD] = pct
    }
}
