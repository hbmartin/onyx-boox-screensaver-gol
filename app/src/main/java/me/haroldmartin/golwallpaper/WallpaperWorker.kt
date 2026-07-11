package me.haroldmartin.golwallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.haroldmartin.golwallpaper.data.UserDataStore.Keys
import me.haroldmartin.golwallpaper.domain.DEFAULT_BATTERY_THRESHOLD_PCT
import me.haroldmartin.golwallpaper.domain.isBelowBatteryThreshold
import me.haroldmartin.golwallpaper.utils.getBatteryLevelPct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "WallpaperWorker"

class WallpaperWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    @Suppress("TooGenericExceptionCaught", "InjectDispatcher")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doWork on thread ${Thread.currentThread().name}")
        try {
            AppContainer.init(context)
            val threshold = AppContainer.userDataStore[Keys.BATTERY_THRESHOLD].first()
                ?: DEFAULT_BATTERY_THRESHOLD_PCT
            if (isBelowBatteryThreshold(context.getBatteryLevelPct(), threshold)) {
                Log.d(TAG, "Skipping update: battery below $threshold%")
                return@withContext Result.success()
            }
            AppContainer.saveScreensaver(context, showHint = false)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data", e)
            Result.failure()
        }
    }
}
