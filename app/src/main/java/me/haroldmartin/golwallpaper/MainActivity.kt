package me.haroldmartin.golwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleUpdate()
        setContent {
            GoLWallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen()
                }
            }
        }
    }

    private fun scheduleUpdate() {
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()

        // TODO: configurable intervals
        val workRequest =
            PeriodicWorkRequestBuilder<WallpaperWorker>(15, TimeUnit.MINUTES) // Min schedule interval is 15mins
//                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            uniqueWorkName = "WallpaperWorker",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = workRequest
        )
    }
}
