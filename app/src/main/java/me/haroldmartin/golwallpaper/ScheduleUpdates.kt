package me.haroldmartin.golwallpaper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "WallpaperWorker"

fun scheduleWallpaperUpdates(context: Context, intervalMins: Long) {
    val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()

    val workRequest =
        PeriodicWorkRequestBuilder<WallpaperWorker>(intervalMins, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInitialDelay(0, TimeUnit.MILLISECONDS)
            .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        uniqueWorkName = UNIQUE_WORK_NAME,
        existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
        request = workRequest,
    )
}
