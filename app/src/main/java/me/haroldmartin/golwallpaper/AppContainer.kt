package me.haroldmartin.golwallpaper

import android.content.Context
import me.haroldmartin.golwallpaper.data.UserDataStore

object AppContainer {
    lateinit var userDataStore: UserDataStore
        private set

    fun init(context: Context) {
        userDataStore = UserDataStore(context = context)
    }
}