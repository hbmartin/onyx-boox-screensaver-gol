package me.haroldmartin.golwallpaper.domain

import kotlinx.coroutines.flow.Flow

fun interface ObserveUiState {
    operator fun invoke(): Flow<UiState>
}
