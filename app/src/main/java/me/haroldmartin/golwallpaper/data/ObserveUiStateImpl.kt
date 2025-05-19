package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.DEFAULT_BG
import me.haroldmartin.golwallpaper.domain.DEFAULT_FG
import me.haroldmartin.golwallpaper.domain.ObserveUiState
import me.haroldmartin.golwallpaper.domain.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveUiStateImpl(val dataStore: UserDataStore) : ObserveUiState {
    override operator fun invoke(): Flow<UiState> = dataStore[UserDataStore.Keys.FG_COLOR]
        .combine(dataStore[UserDataStore.Keys.BG_COLOR]) { fgColor, bgColor ->
            UiState(
                fgColor = fgColor ?: DEFAULT_FG,
                bgColor = bgColor ?: DEFAULT_BG,
            )
        }
}
