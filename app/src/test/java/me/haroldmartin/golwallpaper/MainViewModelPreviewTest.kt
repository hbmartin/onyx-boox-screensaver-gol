package me.haroldmartin.golwallpaper

import me.haroldmartin.golwallpaper.domain.OutputOrientation
import kotlin.test.Test
import kotlin.test.assertEquals

class MainViewModelPreviewTest {
    @Test
    fun `preview geometry preserves valid device resolution`() {
        val geometry = createPreviewGeometry(resolution = 1860 to 2480, cellSize = 10)

        assertEquals(1860 to 2480, geometry.bitmapResolution)
        assertEquals(248, geometry.rows)
        assertEquals(186, geometry.columns)
    }

    @Test
    fun `preview geometry falls back when device resolution is invalid`() {
        val geometry = createPreviewGeometry(resolution = 0 to 0, cellSize = 10)

        assertEquals(360 to 480, geometry.bitmapResolution)
        assertEquals(48, geometry.rows)
        assertEquals(36, geometry.columns)
    }

    @Test
    fun `preview geometry preserves device orientation in auto mode`() {
        val geometry = createPreviewGeometry(
            resolution = 2480 to 1860,
            cellSize = 10,
            outputOrientation = OutputOrientation.AUTO,
        )

        assertEquals(2480 to 1860, geometry.bitmapResolution)
        assertEquals(186, geometry.rows)
        assertEquals(248, geometry.columns)
    }

    @Test
    fun `preview geometry locks portrait orientation`() {
        val geometry = createPreviewGeometry(
            resolution = 2480 to 1860,
            cellSize = 10,
            outputOrientation = OutputOrientation.PORTRAIT,
        )

        assertEquals(1860 to 2480, geometry.bitmapResolution)
        assertEquals(248, geometry.rows)
        assertEquals(186, geometry.columns)
    }

    @Test
    fun `preview geometry locks landscape orientation`() {
        val geometry = createPreviewGeometry(
            resolution = 1860 to 2480,
            cellSize = 10,
            outputOrientation = OutputOrientation.LANDSCAPE,
        )

        assertEquals(2480 to 1860, geometry.bitmapResolution)
        assertEquals(186, geometry.rows)
        assertEquals(248, geometry.columns)
    }
}
