package me.haroldmartin.golwallpaper.utils

data class RenderLayer(
    val grid: Array<BooleanArray>,
    val fgColor: Int,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is RenderLayer && fgColor == other.fgColor && grid.contentDeepEquals(other.grid))

    override fun hashCode(): Int = 31 * grid.contentDeepHashCode() + fgColor
}
