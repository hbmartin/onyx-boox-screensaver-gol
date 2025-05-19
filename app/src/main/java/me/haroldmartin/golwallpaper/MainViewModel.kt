package me.haroldmartin.golwallpaper

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.haroldmartin.golwallpaper.domain.SaveBgColor
import me.haroldmartin.golwallpaper.domain.SaveFgColor
import me.haroldmartin.golwallpaper.domain.UiState
import me.haroldmartin.golwallpaper.utils.SaveScreensaver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5000L

class MainViewModel(
    private val saveBgColor: SaveBgColor,
    private val saveFgColor: SaveFgColor,
    private val saveScreenSaver: SaveScreensaver,
) : ViewModel() {
    val uiState: StateFlow<UiState> = AppContainer.observeUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState(),
        )

    constructor() : this(
        saveBgColor = AppContainer.saveBgColor,
        saveFgColor = AppContainer.saveFgColor,
        saveScreenSaver = AppContainer.saveScreensaver,
    )

    fun setFgColor(context: Context, color: Int) = viewModelScope.launch {
        saveFgColor(color)
        saveScreenSaver(context, showToast = true)
    }

    fun setBgColor(context: Context, color: Int) = viewModelScope.launch {
        saveBgColor(color)
        saveScreenSaver(context, showToast = true)
    }

    fun saveNextStep(context: Context) = viewModelScope.launch {
        saveScreenSaver(context, showToast = true)
    }

    fun reset(context: Context, pattern: String?) = viewModelScope.launch {
        saveScreenSaver(context, showToast = true, pattern = pattern)
    }

    fun openIssues(context: Context) {
        context.startActivity(
            android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                "https://github.com/hbmartin/onyx-boox-screensaver-gol/issues".toUri(),
            ),
        )
    }
}
