package me.haroldmartin.golwallpaper.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

private const val ONYX_SETTINGS_PACKAGE = "com.onyx"
private const val ONYX_SETTINGS_ACTIVITY = "com.onyx.common.setting.ui.SettingContainerActivity"
private const val ONYX_FREEZE_SETTINGS_ACTION = "onyx.settings.action.APP_FREEZE_MANAGEMENT"

fun openOnyxFreezeSettings(context: Context) {
    val intent = Intent(ONYX_FREEZE_SETTINGS_ACTION).apply {
        component = ComponentName(ONYX_SETTINGS_PACKAGE, ONYX_SETTINGS_ACTIVITY)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
    }
}
