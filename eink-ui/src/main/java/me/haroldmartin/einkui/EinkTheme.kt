@file:Suppress("CompositionLocalAllowlist", "PropertyName")

package me.haroldmartin.einkui

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("MagicNumber")
private val DEFAULT_DISABLED_COLOR = Color(0xFF666666)

@Immutable
data class EinkColors(
    val background: Color = Color.White,
    val surface: Color = Color.White,
    val content: Color = Color.Black,
    val outline: Color = Color.Black,
    val disabled: Color = DEFAULT_DISABLED_COLOR,
    val accent: Color = Color.Black,
)

@Immutable
data class EinkTypography(
    val body: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    val label: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    val supporting: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Immutable
data class EinkSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
)

@Immutable
data class EinkBorders(
    val standard: Dp = 1.dp,
    val strong: Dp = 2.dp,
)

@Immutable
data class EinkShapes(
    val surface: CornerBasedShape = RoundedCornerShape(0.dp),
    val control: CornerBasedShape = RoundedCornerShape(4.dp),
)

@Immutable
data class EinkLayoutTokens(
    val minimumTouchTarget: Dp = 48.dp,
    val singlePaneMaxWidth: Dp = 720.dp,
    val dialogMaxWidth: Dp = 1120.dp,
    val dialogWidthFraction: Float = 0.9f,
    val dialogHeightFraction: Float = 0.9f,
    val twoPaneBreakpoint: Dp = 840.dp,
    val twoPaneMinimumHeight: Dp = 480.dp,
    val paneGap: Dp = 24.dp,
    val screenPadding: Dp = 32.dp,
)

object EinkThemeDefaults {
    val colors = EinkColors()
    val typography = EinkTypography()
    val spacing = EinkSpacing()
    val borders = EinkBorders()
    val shapes = EinkShapes()
    val layout = EinkLayoutTokens()
}

private val LocalEinkColors = staticCompositionLocalOf { EinkThemeDefaults.colors }
private val LocalEinkTypography = staticCompositionLocalOf { EinkThemeDefaults.typography }
private val LocalEinkSpacing = staticCompositionLocalOf { EinkThemeDefaults.spacing }
private val LocalEinkBorders = staticCompositionLocalOf { EinkThemeDefaults.borders }
private val LocalEinkShapes = staticCompositionLocalOf { EinkThemeDefaults.shapes }
private val LocalEinkLayout = staticCompositionLocalOf { EinkThemeDefaults.layout }

object EinkTheme {
    val colors: EinkColors
        @Composable get() = LocalEinkColors.current

    val typography: EinkTypography
        @Composable get() = LocalEinkTypography.current

    val spacing: EinkSpacing
        @Composable get() = LocalEinkSpacing.current

    val borders: EinkBorders
        @Composable get() = LocalEinkBorders.current

    val shapes: EinkShapes
        @Composable get() = LocalEinkShapes.current

    val layout: EinkLayoutTokens
        @Composable get() = LocalEinkLayout.current
}

@Composable
@Suppress("LongParameterList")
fun EinkTheme(
    colors: EinkColors = EinkThemeDefaults.colors,
    accent: Color = colors.accent,
    typography: EinkTypography = EinkThemeDefaults.typography,
    spacing: EinkSpacing = EinkThemeDefaults.spacing,
    borders: EinkBorders = EinkThemeDefaults.borders,
    shapes: EinkShapes = EinkThemeDefaults.shapes,
    layout: EinkLayoutTokens = EinkThemeDefaults.layout,
    content: @Composable () -> Unit,
) {
    val resolvedColors = colors.copy(accent = accent)
    CompositionLocalProvider(
        LocalEinkColors provides resolvedColors,
        LocalEinkTypography provides typography,
        LocalEinkSpacing provides spacing,
        LocalEinkBorders provides borders,
        LocalEinkShapes provides shapes,
        LocalEinkLayout provides layout,
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = resolvedColors.content,
                onPrimary = resolvedColors.surface,
                secondary = resolvedColors.content,
                onSecondary = resolvedColors.surface,
                background = resolvedColors.background,
                onBackground = resolvedColors.content,
                surface = resolvedColors.surface,
                onSurface = resolvedColors.content,
                outline = resolvedColors.outline,
                error = resolvedColors.content,
                onError = resolvedColors.surface,
                surfaceVariant = resolvedColors.surface,
                surfaceContainerLowest = resolvedColors.surface,
                surfaceContainerLow = resolvedColors.surface,
                surfaceContainer = resolvedColors.surface,
                surfaceContainerHigh = resolvedColors.surface,
                surfaceContainerHighest = resolvedColors.surface,
            ),
            typography = Typography(
                bodyLarge = typography.body,
                labelLarge = typography.label,
                titleLarge = typography.title,
                bodySmall = typography.supporting,
            ),
            shapes = Shapes(
                extraSmall = shapes.control,
                small = shapes.control,
                medium = shapes.surface,
                large = shapes.surface,
                extraLarge = shapes.surface,
            ),
            content = content,
        )
    }
}
