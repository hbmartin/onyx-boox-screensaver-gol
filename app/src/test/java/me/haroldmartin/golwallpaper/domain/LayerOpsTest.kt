package me.haroldmartin.golwallpaper.domain

import androidx.compose.ui.graphics.toArgb
import me.haroldmartin.golwallpaper.ui.theme.Colors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class LayerOpsTest {
    private val first = Layer(fgColor = 1)
    private val second = Layer(fgColor = 2)
    private val third = Layer(fgColor = 3)

    @Test
    fun `add appends a default layer using the requested color`() {
        val result = LayerOps.addDefault(listOf(first), nextColor = 9)

        assertEquals(listOf(first, Layer(fgColor = 9)), result)
    }

    @Test
    fun `remove cannot empty the list and ignores invalid indexes`() {
        val only = listOf(first)

        assertSame(only, LayerOps.removeAt(only, 0))
        assertSame(only, LayerOps.removeAt(only, 4))
        assertEquals(listOf(first, third), LayerOps.removeAt(listOf(first, second, third), 1))
    }

    @Test
    fun `move up swaps with previous layer and guards bounds`() {
        val layers = listOf(first, second, third)

        assertEquals(listOf(second, first, third), LayerOps.moveUp(layers, 1))
        assertSame(layers, LayerOps.moveUp(layers, 0))
    }

    @Test
    fun `move down swaps with next layer and guards bounds`() {
        val layers = listOf(first, second, third)

        assertEquals(listOf(first, third, second), LayerOps.moveDown(layers, 1))
        assertSame(layers, LayerOps.moveDown(layers, 2))
    }

    @Test
    fun `toggle and update change only the selected layer`() {
        val toggled = LayerOps.setEnabled(listOf(first, second), 1, false)
        val updated = LayerOps.updateAt(toggled, 0) { layer -> layer.copy(rule = "B2/S") }

        assertFalse(updated[1].isEnabled)
        assertEquals("B2/S", updated[0].rule)
        assertEquals(second.copy(isEnabled = false), updated[1])
    }

    @Test
    fun `next unused color chooses palette order and wraps after exhaustion`() {
        val firstPaletteColor = Colors.ALL.first().value.toArgb()
        val paletteLayers = Colors.ALL.map { color -> Layer(fgColor = color.value.toArgb()) }

        assertEquals(firstPaletteColor, LayerOps.nextUnusedColor(emptyList()))
        assertEquals(firstPaletteColor, LayerOps.nextUnusedColor(paletteLayers))
    }
}
