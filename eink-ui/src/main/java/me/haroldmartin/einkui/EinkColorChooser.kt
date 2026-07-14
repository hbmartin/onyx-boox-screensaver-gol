package me.haroldmartin.einkui

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

@Suppress("MagicNumber")
enum class EinkPaletteColor(
    val color: Color,
    @StringRes internal val labelResource: Int,
) {
    Red(Color(0xFFEF5350), R.string.eink_color_red),
    Pink(Color(0xFFBA68C8), R.string.eink_color_pink),
    Purple(Color(0xFF673AB7), R.string.eink_color_purple),
    Cyan(Color(0xFF00ACC1), R.string.eink_color_cyan),
    Green(Color(0xFF4CAF50), R.string.eink_color_green),
    Yellow(Color(0xFFFFEB3B), R.string.eink_color_yellow),
    BlueGray(Color(0xFF546E7A), R.string.eink_color_blue_gray),
    Black(Color(0xFF212121), R.string.eink_color_black),
    White(Color(0xFFFFFFFF), R.string.eink_color_white),
}

sealed interface EinkColorChoice {
    data class Palette(val value: EinkPaletteColor) : EinkColorChoice

    data object Random : EinkColorChoice
}

@Composable
fun EinkPaletteColor.displayName(): String = stringResource(labelResource)

@Composable
fun EinkColorChooser(
    selection: EinkColorChoice?,
    onSelectionChange: (EinkColorChoice) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showRandom: Boolean = true,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
    ) {
        EinkPaletteColor.entries.forEach { paletteColor ->
            ColorSwatch(
                color = paletteColor.color,
                description = paletteColor.displayName(),
                selected = selection == EinkColorChoice.Palette(paletteColor),
                enabled = enabled,
                onClick = { onSelectionChange(EinkColorChoice.Palette(paletteColor)) },
            )
        }
        if (showRandom) {
            RandomSwatch(
                selected = selection == EinkColorChoice.Random,
                enabled = enabled,
                onClick = { onSelectionChange(EinkColorChoice.Random) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val outline = when {
        !enabled -> EinkTheme.colors.disabled
        selected -> EinkTheme.colors.accent
        else -> EinkTheme.colors.outline
    }
    val borderWidth = if (selected) EinkTheme.borders.strong else EinkTheme.borders.standard
    val checkColor = if (color.luminance() > LIGHT_COLOR_THRESHOLD) Color.Black else Color.White
    Box(
        modifier = Modifier
            .size(EinkTheme.layout.minimumTouchTarget)
            .clip(EinkTheme.shapes.control)
            .background(color)
            .border(borderWidth, outline, EinkTheme.shapes.control)
            .einkClickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) SelectedCheckmark(checkColor)
    }
}

@Composable
private fun RandomSwatch(selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val description = stringResource(R.string.eink_random)
    val outline = when {
        !enabled -> EinkTheme.colors.disabled
        selected -> EinkTheme.colors.accent
        else -> EinkTheme.colors.outline
    }
    val borderWidth = if (selected) EinkTheme.borders.strong else EinkTheme.borders.standard
    Box(
        modifier = Modifier
            .height(EinkTheme.layout.minimumTouchTarget)
            .widthIn(min = EinkTheme.layout.minimumTouchTarget)
            .clip(EinkTheme.shapes.control)
            .background(EinkTheme.colors.surface)
            .border(borderWidth, outline, EinkTheme.shapes.control)
            .einkClickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            SelectedCheckmark(EinkTheme.colors.accent)
        } else {
            Text(
                text = description,
                modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
                color = if (enabled) EinkTheme.colors.content else EinkTheme.colors.disabled,
                fontWeight = FontWeight.Bold,
                style = EinkTheme.typography.supporting,
            )
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun SelectedCheckmark(color: Color) {
    val strokeWidth = EinkTheme.borders.strong
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(EinkTheme.spacing.medium),
    ) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.05f, size.height * 0.55f),
            end = Offset(size.width * 0.35f, size.height * 0.85f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Square,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.85f),
            end = Offset(size.width * 0.95f, size.height * 0.1f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Square,
        )
    }
}

private const val LIGHT_COLOR_THRESHOLD = 0.5f
