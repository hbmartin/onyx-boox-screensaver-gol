package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MIN_TEXT_CONTRAST_RATIO = 4.5f
private const val LUMINANCE_OFFSET = 0.05f

class ThemeTest {
    @Test
    fun `primary action color contrasts with pale surfaces`() {
        val backgrounds = listOf(
            COLOR_SCHEME.background,
            COLOR_SCHEME.surfaceContainerHigh,
        )

        backgrounds.forEach { background ->
            val contrastRatio = contrastRatio(COLOR_SCHEME.primary, background)
            assertTrue(
                "Expected primary action contrast of at least $MIN_TEXT_CONTRAST_RATIO, " +
                    "but was $contrastRatio",
                contrastRatio >= MIN_TEXT_CONTRAST_RATIO,
            )
        }
    }
}

private fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + LUMINANCE_OFFSET) / (darker + LUMINANCE_OFFSET)
}
