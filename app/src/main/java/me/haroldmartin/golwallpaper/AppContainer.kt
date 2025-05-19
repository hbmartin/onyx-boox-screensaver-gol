package me.haroldmartin.golwallpaper

import android.content.Context
import me.haroldmartin.golwallpaper.data.ObserveUiStateImpl
import me.haroldmartin.golwallpaper.data.SaveBgColorImpl
import me.haroldmartin.golwallpaper.data.SaveFgColorImpl
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.utils.SaveScreensaver
import kotlinx.coroutines.Dispatchers

@Suppress("LateinitUsage", "AvoidVarsExceptWithDelegate", "InjectDispatcher")
object AppContainer {
    lateinit var userDataStore: UserDataStore
        private set

    val observeUiState get() = ObserveUiStateImpl(userDataStore)
    val saveBgColor get() = SaveBgColorImpl(userDataStore)
    val saveFgColor get() = SaveFgColorImpl(userDataStore)
    val saveScreensaver get() = SaveScreensaver(userDataStore, Dispatchers.IO)

    fun init(context: Context) {
        if (!::userDataStore.isInitialized) {
            userDataStore = UserDataStore(context = context)
        }
    }
}
