package me.haroldmartin.golwallpaper

import android.app.Application

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this.applicationContext)
    }
}