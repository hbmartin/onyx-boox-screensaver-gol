package me.haroldmartin.golwallpaper.domain

const val DEFAULT_CELL_SIZE = 10
const val DEFAULT_UPDATE_INTERVAL_MINS = 15L
const val DEFAULT_RULE = "B3/S23"
const val DEFAULT_SHOW_STATS = false

enum class WallpaperTarget {
    LOCK,
    HOME,
    BOTH,
    ;

    companion object {
        fun fromString(value: String?): WallpaperTarget =
            entries.firstOrNull { it.name == value } ?: LOCK
    }
}

enum class RulePreset(val displayName: String, val rule: String) {
    CONWAY("Conway's Life", "B3/S23"),
    HIGH_LIFE("HighLife", "B36/S23"),
    DAY_AND_NIGHT("Day & Night", "B3678/S34678"),
    SEEDS("Seeds", "B2/S"),
    LIFE_WITHOUT_DEATH("Life without Death", "B3/S012345678"),
}

data class GolSettings(
    val cellSize: Int = DEFAULT_CELL_SIZE,
    val updateIntervalMins: Long = DEFAULT_UPDATE_INTERVAL_MINS,
    val rule: String = DEFAULT_RULE,
    val isStatsVisible: Boolean = DEFAULT_SHOW_STATS,
    val wallpaperTarget: WallpaperTarget = WallpaperTarget.LOCK,
)
