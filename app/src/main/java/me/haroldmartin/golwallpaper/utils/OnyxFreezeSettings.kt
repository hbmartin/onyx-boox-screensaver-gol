package me.haroldmartin.golwallpaper.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

private const val TAG = "OnyxFreezeSettings"
private const val ONYX_SETTINGS_PACKAGE = "com.onyx"
private const val ONYX_SETTINGS_ACTIVITY = "com.onyx.common.setting.ui.SettingContainerActivity"
private const val ONYX_FREEZE_SETTINGS_ACTION = "onyx.settings.action.APP_FREEZE_MANAGEMENT"
private const val ONYX_EINK_HELPER_CLASS = "android.onyx.optimization.EInkHelper"
private const val GET_AUTO_FREEZE_APPS_METHOD = "getAutoFreezeApps"

fun openOnyxFreezeSettings(context: Context) {
    val intent = Intent(ONYX_FREEZE_SETTINGS_ACTION).apply {
        component = ComponentName(ONYX_SETTINGS_PACKAGE, ONYX_SETTINGS_ACTIVITY)
    }
    launchWithFallback(
        launch = { context.startActivity(intent) },
        fallback = { context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS)) },
    )
}

@Suppress("NoCallbacksInFunctions")
internal inline fun launchWithFallback(launch: () -> Unit, fallback: () -> Unit) {
    try {
        launch()
    } catch (_: ActivityNotFoundException) {
        fallback()
    } catch (_: SecurityException) {
        fallback()
    }
}

fun isOnyxAutoFreezeEnabled(packageName: String): Boolean? =
    isPackageAutoFrozen(getOnyxAutoFreezeApps(), packageName)

private fun getOnyxAutoFreezeApps(): Any? =
    try {
        Class.forName(ONYX_EINK_HELPER_CLASS)
            .getMethod(GET_AUTO_FREEZE_APPS_METHOD)
            .invoke(null)
    } catch (exception: ReflectiveOperationException) {
        Log.d(TAG, "BOOX auto-freeze API is unavailable", exception)
        null
    } catch (exception: SecurityException) {
        Log.d(TAG, "BOOX auto-freeze API is inaccessible", exception)
        null
    } catch (error: LinkageError) {
        Log.d(TAG, "BOOX auto-freeze API could not be linked", error)
        null
    }

internal fun isPackageAutoFrozen(autoFreezeApps: Any?, packageName: String): Boolean? =
    when (autoFreezeApps) {
        is Collection<*> -> autoFreezeApps.asPackageNames()?.contains(packageName)
        is Array<*> -> autoFreezeApps.asList().asPackageNames()?.contains(packageName)
        else -> null
    }

private fun Collection<*>.asPackageNames(): List<String>? =
    takeIf { entries -> entries.all { it is String } }?.filterIsInstance<String>()
