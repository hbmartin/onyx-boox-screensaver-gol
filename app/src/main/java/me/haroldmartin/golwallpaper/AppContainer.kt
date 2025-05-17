package me.haroldmartin.golwallpaper

import android.content.Context
import me.haroldmartin.golwallpaper.data.UserDataStore

@Suppress("LateinitUsage", "AvoidVarsExceptWithDelegate")
object AppContainer {
    lateinit var userDataStore: UserDataStore
        private set

    fun init(context: Context) {
        if (!::userDataStore.isInitialized) {
            userDataStore = UserDataStore(context = context)
        }
    }
}
