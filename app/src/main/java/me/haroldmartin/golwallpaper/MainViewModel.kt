package me.haroldmartin.golwallpaper

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.utils.getScreenResolution
import me.haroldmartin.golwallpaper.utils.saveWallpaper
import me.haroldmartin.golwallpaper.utils.toRowsCols
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

data class UiState(val fgColor: Int, val bgColor: Int)

class MainViewModel : ViewModel() {
    private val repository: UserDataStore = AppContainer.userDataStore
    val uiState: StateFlow<UiState> = repository[UserDataStore.Keys.FG_COLOR]
        .combine(repository[UserDataStore.Keys.BG_COLOR]) { fgColor, bgColor ->
            UiState(
                fgColor = fgColor ?: Color.Black.toArgb(),
                bgColor = bgColor ?: Color.White.toArgb(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(
                fgColor = Color.Black.toArgb(),
                bgColor = Color.White.toArgb(),
            ),
        )

    fun setFgColor(context: Context, color: Int) {
        Log.d(TAG, "setFgColor: $color")
        viewModelScope.launch {
            repository[UserDataStore.Keys.FG_COLOR] = color
            updateGameImage(context)
        }
    }

    fun setBgColor(context: Context, color: Int) {
        Log.d(TAG, "setBgColor: $color")
        viewModelScope.launch {
            repository[UserDataStore.Keys.BG_COLOR] = color
            updateGameImage(context)
        }
    }

    fun updateGameImage(context: Context) = viewModelScope.launch {
        saveWallpaper(context, repository, showToast = true)
    }

    fun reset(context: Context, pattern: String?) = viewModelScope.launch {
        val resolution = getScreenResolution(context)
        val (rows, cols) = resolution.toRowsCols()

        repository[UserDataStore.Keys.GAME_STATE] = GolController(rows, cols, pattern).toString()
        viewModelScope.launch {
            saveWallpaper(context, repository, showToast = true, updateGame = false)
        }
    }
}
