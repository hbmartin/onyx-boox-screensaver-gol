package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.DEFAULT_BATTERY_THRESHOLD_PCT
import me.haroldmartin.golwallpaper.domain.DEFAULT_BG
import me.haroldmartin.golwallpaper.domain.DEFAULT_CELL_SIZE
import me.haroldmartin.golwallpaper.domain.DEFAULT_SHOW_STATS
import me.haroldmartin.golwallpaper.domain.DEFAULT_UPDATE_INTERVAL_MINS
import me.haroldmartin.golwallpaper.domain.GolSettings
import me.haroldmartin.golwallpaper.domain.ObserveUiState
import me.haroldmartin.golwallpaper.domain.UiState
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveUiStateImpl(val dataStore: UserDataStore) : ObserveUiState {
    override operator fun invoke(): Flow<UiState> = combine(
        dataStore[UserDataStore.Keys.LAYERS],
        dataStore[UserDataStore.Keys.BG_COLOR],
        observeSettings(),
    ) { layers, bgColor, settings ->
        UiState(
            bgColor = bgColor ?: DEFAULT_BG,
            settings = settings,
            layers = LayersSerializer.decode(layers),
        )
    }

    private fun observeSettings(): Flow<GolSettings> = combine(
        combine(
            flow = dataStore[UserDataStore.Keys.CELL_SIZE],
            flow2 = dataStore[UserDataStore.Keys.UPDATE_INTERVAL_MINS],
            flow3 = dataStore[UserDataStore.Keys.SHOW_STATS],
            flow4 = dataStore[UserDataStore.Keys.WALLPAPER_TARGET],
        ) { cellSize, intervalMins, showStats, target ->
            GolSettings(
                cellSize = cellSize ?: DEFAULT_CELL_SIZE,
                updateIntervalMins = intervalMins ?: DEFAULT_UPDATE_INTERVAL_MINS,
                isStatsVisible = showStats ?: DEFAULT_SHOW_STATS,
                wallpaperTarget = WallpaperTarget.fromString(target),
            )
        },
        dataStore[UserDataStore.Keys.BATTERY_THRESHOLD],
    ) { settings, batteryThreshold ->
        settings.copy(
            batteryThresholdPct = batteryThreshold ?: DEFAULT_BATTERY_THRESHOLD_PCT,
        )
    }
}
