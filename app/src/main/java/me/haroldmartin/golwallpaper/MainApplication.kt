package me.haroldmartin.golwallpaper

import android.app.Application
import android.os.StrictMode
import android.util.Log
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.domain.DEFAULT_UPDATE_INTERVAL_MINS
import me.haroldmartin.golwallpaper.utils.getAppMemoryUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "MainApplication"

class MainApplication : Application() {
    @Suppress("InjectDispatcher")
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        configureStrictMode()
        AppContainer.init(this.applicationContext)
        applicationScope.launch {
            val intervalMins =
                AppContainer.userDataStore[UserDataStore.Keys.UPDATE_INTERVAL_MINS].first()
                    ?: DEFAULT_UPDATE_INTERVAL_MINS
            scheduleWallpaperUpdates(this@MainApplication, intervalMins)
        }
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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory: ($level) ${getAppMemoryUsage(this)}")
    }
}
