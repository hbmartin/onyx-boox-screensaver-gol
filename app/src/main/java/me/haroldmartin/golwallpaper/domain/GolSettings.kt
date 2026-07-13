package me.haroldmartin.golwallpaper.domain

const val DEFAULT_CELL_SIZE = 10
const val DEFAULT_UPDATE_INTERVAL_MINS = 15L
const val DEFAULT_RULE = "B3/S23"
const val DEFAULT_SHOW_STATS = false
const val DEFAULT_BATTERY_THRESHOLD_PCT = 0
const val DEFAULT_WRAP_EDGES = true

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

enum class OutputOrientation {
    AUTO,
    PORTRAIT,
    LANDSCAPE,
    ;

    companion object {
        fun fromString(value: String?): OutputOrientation =
            entries.firstOrNull { it.name == value } ?: AUTO
    }
}

enum class RulePreset(val rule: String) {
    CONWAY("B3/S23"),
    HIGH_LIFE("B36/S23"),
    DAY_AND_NIGHT("B3678/S34678"),
    SEEDS("B2/S"),
    LIFE_WITHOUT_DEATH("B3/S012345678"),
}

data class GolSettings(
    val cellSize: Int = DEFAULT_CELL_SIZE,
    val updateIntervalMins: Long = DEFAULT_UPDATE_INTERVAL_MINS,
    val isStatsVisible: Boolean = DEFAULT_SHOW_STATS,
    val wallpaperTarget: WallpaperTarget = WallpaperTarget.LOCK,
    val batteryThresholdPct: Int = DEFAULT_BATTERY_THRESHOLD_PCT,
    val areEdgesWrapped: Boolean = DEFAULT_WRAP_EDGES,
    val outputOrientation: OutputOrientation = OutputOrientation.AUTO,
)
