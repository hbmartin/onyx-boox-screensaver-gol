package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.os.BatteryManager

const val BATTERY_LEVEL_UNKNOWN = -1

/**
 * Returns the current battery charge as a percentage (0..100), or
 * [BATTERY_LEVEL_UNKNOWN] when the device cannot report it. Reading the
 * capacity property requires no manifest permission.
 */
fun Context.getBatteryLevelPct(): Int =
    getSystemService(BatteryManager::class.java)
        ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        ?: BATTERY_LEVEL_UNKNOWN
