package me.haroldmartin.golwallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.haroldmartin.golwallpaper.utils.saveWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "WallpaperWorker"

class WallpaperWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doWork on thread ${Thread.currentThread().name}")
        try {
            AppContainer.init(context)
            saveWallpaper(context, AppContainer.userDataStore, false)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data", e)
            Result.failure()
        }
    }
}
