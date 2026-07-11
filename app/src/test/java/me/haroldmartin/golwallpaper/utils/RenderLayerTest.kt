package me.haroldmartin.golwallpaper.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RenderLayerTest {
    @Test
    fun `equality compares grid contents and foreground color`() {
        val first = RenderLayer(
            grid = arrayOf(booleanArrayOf(true, false), booleanArrayOf(false, true)),
            fgColor = 1,
        )
        val sameContents = RenderLayer(
            grid = arrayOf(booleanArrayOf(true, false), booleanArrayOf(false, true)),
            fgColor = 1,
        )
        val differentGrid = RenderLayer(
            grid = arrayOf(booleanArrayOf(false, false), booleanArrayOf(false, true)),
            fgColor = 1,
        )

        assertEquals(first, sameContents)
        assertEquals(first.hashCode(), sameContents.hashCode())
        assertNotEquals(first, differentGrid)
        assertNotEquals(first, first.copy(fgColor = 2))
    }
}
