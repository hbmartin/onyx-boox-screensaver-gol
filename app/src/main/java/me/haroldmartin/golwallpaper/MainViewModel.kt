package me.haroldmartin.golwallpaper

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.utils.saveWallpaper

private const val TAG = "GolViewModel"

class MainViewModel() : ViewModel() {
    private val repository: UserDataStore = AppContainer.userDataStore

    fun setFgColor(context: Context, color: Colors) {
        viewModelScope.launch {
            repository[UserDataStore.Keys.FG_COLOR] = color.value.toArgb()
            updateGameImage(context)
        }
    }

    fun updateGameImage(context: Context) = viewModelScope.launch {
        saveWallpaper(context, repository, showToast = true)
    }
}