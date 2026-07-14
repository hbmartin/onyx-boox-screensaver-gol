package me.haroldmartin.einkui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MINIMUM_TEXT_CONTRAST = 4.5f
private const val LUMINANCE_OFFSET = 0.05f

class EinkThemeTest {
    @Test
    fun `default content and disabled colors contrast with the surface`() {
        val colors = EinkThemeDefaults.colors

        listOf(colors.content, colors.disabled).forEach { foreground ->
            assertTrue(contrastRatio(foreground, colors.surface) >= MINIMUM_TEXT_CONTRAST)
        }
    }

    @Test
    fun `fixed palette preserves the app palette`() {
        assertEquals(
            listOf(
                Color(0xFFEF5350),
                Color(0xFFBA68C8),
                Color(0xFF673AB7),
                Color(0xFF00ACC1),
                Color(0xFF4CAF50),
                Color(0xFFFFEB3B),
                Color(0xFF546E7A),
                Color(0xFF212121),
                Color(0xFFFFFFFF),
            ),
            EinkPaletteColor.entries.map(EinkPaletteColor::color),
        )
    }
}

private fun contrastRatio(first: Color, second: Color): Float {
    val lighter = maxOf(first.luminance(), second.luminance())
    val darker = minOf(first.luminance(), second.luminance())
    return (lighter + LUMINANCE_OFFSET) / (darker + LUMINANCE_OFFSET)
}
