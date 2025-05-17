package me.haroldmartin.golwallpaper

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.haroldmartin.golwallpaper.utils.getAppMemoryUsage
import java.util.concurrent.TimeUnit

private const val TAG = "MainApplication"
private const val UPDATE_TIME_MINS = 15L

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        configureStrictMode()
        AppContainer.init(this.applicationContext)
        scheduleUpdate()
        Log.d(TAG, "isAppEnabled: ${isAppEnabled(this, "me.haroldmartin.golwallpaper")}")
    }

    private fun configureStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectImplicitDirectBoot()
                .penaltyLog()
                .build(),
        )
    }

    private fun scheduleUpdate() {
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()

        val workRequest =
            PeriodicWorkRequestBuilder<WallpaperWorker>(UPDATE_TIME_MINS, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            uniqueWorkName = "WallpaperWorker",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = workRequest,
        )

        val workInfo = WorkManager.getInstance(this)
            .getWorkInfoById(workRequest.id)
            .get()
        Log.d(TAG, "worker info: $workInfo")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory: ($level) ${getAppMemoryUsage(this)}")
    }
}

private fun isAppEnabled(context: Context, packageName: String): Boolean = try {
    val pm = context.packageManager
    when (pm.getApplicationEnabledSetting(packageName)) {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
        -> false

        else -> true
    }
} catch (e: IllegalArgumentException) {
    Log.e(TAG, "Invalid package name: $packageName", e)
    false
}
