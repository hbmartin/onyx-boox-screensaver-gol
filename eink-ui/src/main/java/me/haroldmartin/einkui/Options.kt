package me.haroldmartin.einkui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

@Composable
@Suppress("MagicNumber")
fun EinkChoiceButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val accent = EinkTheme.colors.accent
    val strokeWidth = EinkTheme.borders.strong
    EinkButton(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        enabled = enabled,
        role = Role.RadioButton,
        emphasis = if (selected) EinkButtonEmphasis.Strong else EinkButtonEmphasis.Standard,
    ) {
        if (selected) {
            Canvas(
                modifier = Modifier
                    .padding(end = EinkTheme.spacing.extraSmall)
                    .size(EinkTheme.spacing.medium),
            ) {
                drawLine(
                    color = accent,
                    start = Offset(size.width * 0.05f, size.height * 0.55f),
                    end = Offset(size.width * 0.35f, size.height * 0.85f),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = accent,
                    start = Offset(size.width * 0.35f, size.height * 0.85f),
                    end = Offset(size.width * 0.95f, size.height * 0.1f),
                    strokeWidth = strokeWidth.toPx(),
                    cap = StrokeCap.Square,
                )
            }
        }
        content()
    }
}

@Composable
@Suppress("LongParameterList")
fun <T> EinkOptionGroup(
    options: List<T>,
    selected: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    optionLabel: @Composable (T) -> String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraSmall),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = EinkTheme.typography.label,
                fontWeight = FontWeight.Bold,
            )
        }
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraSmall),
        ) {
            options.forEach { option ->
                EinkChoiceButton(
                    selected = option == selected,
                    onClick = { onSelectionChange(option) },
                ) {
                    Text(optionLabel(option))
                }
            }
        }
    }
}
