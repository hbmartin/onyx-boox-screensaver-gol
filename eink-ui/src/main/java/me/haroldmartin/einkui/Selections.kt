package me.haroldmartin.einkui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState

@Composable
@Suppress("MagicNumber")
fun EinkCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val color = if (enabled) EinkTheme.colors.content else EinkTheme.colors.disabled
    val checkStrokeWidth = EinkTheme.borders.strong
    val resolvedModifier = if (onCheckedChange != null) {
        modifier
            .sizeIn(
                minWidth = EinkTheme.layout.minimumTouchTarget,
                minHeight = EinkTheme.layout.minimumTouchTarget,
            )
            .einkClickable(enabled = enabled, role = Role.Checkbox) {
                onCheckedChange(!checked)
            }
            .semantics {
                toggleableState = ToggleableState(checked)
                if (!enabled) disabled()
            }
    } else {
        modifier
    }
    Box(modifier = resolvedModifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(EinkTheme.spacing.large)
                .clip(EinkTheme.shapes.control)
                .border(EinkTheme.borders.standard, color, EinkTheme.shapes.control),
        ) {
            if (checked) {
                Canvas(modifier = Modifier.fillMaxSize().padding(EinkTheme.spacing.extraSmall)) {
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.1f, size.height * 0.55f),
                        end = Offset(size.width * 0.4f, size.height * 0.85f),
                        strokeWidth = checkStrokeWidth.toPx(),
                        cap = StrokeCap.Square,
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.4f, size.height * 0.85f),
                        end = Offset(size.width * 0.95f, size.height * 0.1f),
                        strokeWidth = checkStrokeWidth.toPx(),
                        cap = StrokeCap.Square,
                    )
                }
            }
        }
    }
}

@Composable
fun EinkCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = EinkTheme.layout.minimumTouchTarget)
            .einkClickable(enabled = enabled, role = Role.Checkbox) {
                onCheckedChange(!checked)
            }
            .semantics(mergeDescendants = true) {
                toggleableState = ToggleableState(checked)
                if (!enabled) disabled()
            },
        horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EinkCheckbox(checked = checked, onCheckedChange = null, enabled = enabled)
        content()
    }
}

@Composable
@Suppress("CognitiveComplexMethod")
fun EinkSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = EinkTheme.colors
    val outline = if (enabled) colors.outline else colors.disabled
    val resolvedModifier = if (onCheckedChange != null) {
        modifier
            .sizeIn(
                minWidth = EinkTheme.layout.minimumTouchTarget,
                minHeight = EinkTheme.layout.minimumTouchTarget,
            )
            .einkClickable(enabled = enabled, role = Role.Switch) {
                onCheckedChange(!checked)
            }
            .semantics {
                toggleableState = ToggleableState(checked)
                if (!enabled) disabled()
            }
    } else {
        modifier
    }
    Box(modifier = resolvedModifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = EinkTheme.layout.minimumTouchTarget, height = EinkTheme.spacing.extraLarge)
                .clip(EinkTheme.shapes.control)
                .border(EinkTheme.borders.standard, outline, EinkTheme.shapes.control),
        ) {
            Box(
                modifier = Modifier
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(EinkTheme.spacing.extraSmall)
                    .size(EinkTheme.spacing.large)
                    .clip(EinkTheme.shapes.control)
                    .background(if (checked && enabled) colors.accent else colors.surface)
                    .border(EinkTheme.borders.standard, outline, EinkTheme.shapes.control),
            )
        }
    }
}

@Composable
fun EinkSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = EinkTheme.layout.minimumTouchTarget)
            .einkClickable(enabled = enabled, role = Role.Switch) {
                onCheckedChange(!checked)
            }
            .semantics(mergeDescendants = true) {
                toggleableState = ToggleableState(checked)
                if (!enabled) disabled()
            },
        horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EinkSwitch(checked = checked, onCheckedChange = null, enabled = enabled)
        content()
    }
}

@Composable
fun EinkRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = EinkTheme.colors
    val color = when {
        !enabled -> colors.disabled
        selected -> colors.accent
        else -> colors.outline
    }
    val radioBorderWidth = EinkTheme.borders.standard
    val resolvedModifier = if (onClick != null) {
        modifier
            .sizeIn(
                minWidth = EinkTheme.layout.minimumTouchTarget,
                minHeight = EinkTheme.layout.minimumTouchTarget,
            )
            .einkClickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                this.selected = selected
                if (!enabled) disabled()
            }
    } else {
        modifier
    }
    Box(modifier = resolvedModifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(EinkTheme.spacing.large)) {
            drawCircle(
                color = color,
                style = Stroke(width = radioBorderWidth.toPx()),
            )
            if (selected) {
                drawCircle(color = color, radius = size.minDimension * 0.25f)
            }
        }
    }
}

@Composable
fun EinkRadioRow(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = EinkTheme.layout.minimumTouchTarget)
            .einkClickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) {
                this.selected = selected
                if (!enabled) disabled()
            },
        horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EinkRadioButton(selected = selected, onClick = null, enabled = enabled)
        content()
    }
}

@Composable
fun <T> EinkRadioGroup(
    options: List<T>,
    selected: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionContent: @Composable RowScope.(T) -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraSmall),
    ) {
        options.forEach { option ->
            EinkRadioRow(
                selected = option == selected,
                onClick = { onSelectionChange(option) },
            ) {
                optionContent(option)
            }
        }
    }
}
