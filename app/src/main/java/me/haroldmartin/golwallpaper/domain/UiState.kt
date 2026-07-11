package me.haroldmartin.golwallpaper.domain

const val DEFAULT_FG = 0xFF000000.toInt()
const val DEFAULT_BG = 0xFFFFFFFF.toInt()

data class UiState(
    val bgColor: Int = DEFAULT_BG,
    val settings: GolSettings = GolSettings(),
    val calendarOverlaySettings: CalendarOverlaySettings = CalendarOverlaySettings(),
    val layers: List<Layer> = listOf(Layer()),
)
