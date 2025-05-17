package me.haroldmartin.golwallpaper

import android.app.Application
import android.os.StrictMode

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )

        AppContainer.init(this.applicationContext)
    }
}
