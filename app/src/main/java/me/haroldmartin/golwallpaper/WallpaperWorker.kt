package me.haroldmartin.golwallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.utils.saveWallpaper

private const val TAG = "WallpaperWorker"

class WallpaperWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doWork")
        try {
            AppContainer.init(context)
            val repository: UserDataStore = AppContainer.userDataStore
            Log.d(TAG, "initialized")
            saveWallpaper(context, repository, false)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data", e)
            Result.failure()
        }
    }
}