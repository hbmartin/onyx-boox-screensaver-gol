package me.haroldmartin.golwallpaper.utils

import me.haroldmartin.golwallpaper.domain.CalendarAgenda
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings

data class RenderLayer(
    val grid: Array<BooleanArray>,
    val fgColor: Int,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is RenderLayer && fgColor == other.fgColor && grid.contentDeepEquals(other.grid))

    override fun hashCode(): Int = 31 * grid.contentDeepHashCode() + fgColor
}

data class RenderStats(
    val generation: Int,
    val population: Int,
)

data class DeviceOverlays(
    val calendarAgenda: CalendarAgenda?,
    val calendarSettings: CalendarOverlaySettings,
    val stats: RenderStats?,
)
