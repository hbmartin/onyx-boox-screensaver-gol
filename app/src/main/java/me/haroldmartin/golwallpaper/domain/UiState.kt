package me.haroldmartin.golwallpaper.domain

const val DEFAULT_FG = 0xFF000000.toInt()
const val DEFAULT_BG = 0xFFFFFFFF.toInt()

data class UiState(val fgColor: Int = DEFAULT_FG, val bgColor: Int = DEFAULT_BG)
