package me.haroldmartin.golwallpaper.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolutionTest {
    @Test
    fun `portrait grid uses height for rows and width for columns`() {
        assertEquals(80 to 60, Resolution(1200, 1600).toRowsCols(cellSize = 20))
    }

    @Test
    fun `landscape grid uses height for rows and width for columns`() {
        assertEquals(60 to 80, Resolution(1600, 1200).toRowsCols(cellSize = 20))
    }

    @Test
    fun `grid keeps at least one row and column`() {
        assertEquals(1 to 1, Resolution(20, 30).toRowsCols(cellSize = 40))
    }

    @Test
    fun `grid rejects a non-positive cell size`() {
        assertFailsWith<IllegalArgumentException> {
            Resolution(1200, 1600).toRowsCols(cellSize = 0)
        }
    }

    @Test
    fun `portrait preview preserves aspect ratio within bounds`() {
        assertEquals(240 to 480, Resolution(800, 1600).scaledToFit(360, 480))
    }

    @Test
    fun `landscape preview preserves aspect ratio within bounds`() {
        assertEquals(360 to 270, Resolution(1600, 1200).scaledToFit(360, 480))
    }
}
