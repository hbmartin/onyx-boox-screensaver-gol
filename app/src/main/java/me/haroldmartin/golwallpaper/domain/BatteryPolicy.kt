package me.haroldmartin.golwallpaper.domain

/**
 * Decides whether a background wallpaper update should be skipped to save power.
 *
 * A [thresholdPct] of 0 (or negative) disables the feature — updates never skip.
 * An unknown battery level (negative [batteryLevelPct], e.g. -1) fails open and
 * never skips, so a device that cannot report its level keeps updating.
 */
fun isBelowBatteryThreshold(batteryLevelPct: Int, thresholdPct: Int): Boolean =
    thresholdPct > 0 && batteryLevelPct in 0 until thresholdPct
