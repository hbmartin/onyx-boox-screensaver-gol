package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.einkui.EinkCard
import me.haroldmartin.einkui.EinkDisclosureRow
import me.haroldmartin.einkui.EinkIconButton
import me.haroldmartin.einkui.EinkTheme
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.RulePreset

private val LAYER_ACTION_BUTTON_SIZE = 48.dp
private val LAYER_ACTION_ICON_SIZE = 28.dp
private val LAYER_ACTION_STROKE_WIDTH = 4.dp

private enum class LayerActionIcon {
    UP,
    DOWN,
    REMOVE,
}

@Composable
fun LayerCard(
    layer: Layer,
    index: Int,
    layerCount: Int,
    callbacks: LayerCallbacks,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    EinkCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(EinkTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
        ) {
            LayerHeader(
                index = index,
                layerCount = layerCount,
                isExpanded = isExpanded,
                callbacks = callbacks,
                onToggleExpansion = { isExpanded = !isExpanded },
            )
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(start = EinkTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
                ) {
                    ColorPicker(
                        label = stringResource(R.string.fg_color),
                        selectedColor = layer.fgColor,
                        onClick = callbacks.onColorChange,
                    )
                    RulePicker(
                        selectedRule = layer.rule,
                        onSelect = callbacks.onRuleChange,
                    )
                    PatternPicker(
                        selectedPattern = layer.startingPattern,
                        onClick = callbacks.onResetPattern,
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerHeader(
    index: Int,
    layerCount: Int,
    isExpanded: Boolean,
    callbacks: LayerCallbacks,
    onToggleExpansion: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EinkDisclosureRow(
            expanded = isExpanded,
            onExpandedChange = { onToggleExpansion() },
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.layer_number, index + 1),
                modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
                fontWeight = FontWeight.Bold,
                style = EinkTheme.typography.title,
            )
        }
        LayerIconButton(
            icon = LayerActionIcon.UP,
            description = stringResource(R.string.move_layer_up),
            enabled = index > 0,
            onClick = callbacks.onMoveUp,
        )
        LayerIconButton(
            icon = LayerActionIcon.DOWN,
            description = stringResource(R.string.move_layer_down),
            enabled = index < layerCount - 1,
            onClick = callbacks.onMoveDown,
        )
        LayerIconButton(
            icon = LayerActionIcon.REMOVE,
            description = stringResource(R.string.delete_layer),
            enabled = layerCount > 1,
            onClick = callbacks.onDelete,
        )
    }
}

@Composable
private fun LayerIconButton(
    icon: LayerActionIcon,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    EinkIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(LAYER_ACTION_BUTTON_SIZE)
            .semantics { contentDescription = description },
    ) {
        LayerActionGlyph(icon)
    }
}

@Composable
@Suppress("LongMethod", "MagicNumber")
private fun LayerActionGlyph(icon: LayerActionIcon) {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(LAYER_ACTION_ICON_SIZE)) {
        val left = size.width * 0.2f
        val centerX = size.width * 0.5f
        val right = size.width * 0.8f
        val top = size.height * 0.2f
        val centerY = size.height * 0.5f
        val bottom = size.height * 0.8f
        val strokeWidth = LAYER_ACTION_STROKE_WIDTH.toPx()

        when (icon) {
            LayerActionIcon.UP -> {
                drawLine(
                    color = color,
                    start = Offset(centerX, bottom),
                    end = Offset(centerX, top),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(centerX, top),
                    end = Offset(left, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(centerX, top),
                    end = Offset(right, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            LayerActionIcon.DOWN -> {
                drawLine(
                    color = color,
                    start = Offset(centerX, top),
                    end = Offset(centerX, bottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(centerX, bottom),
                    end = Offset(left, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(centerX, bottom),
                    end = Offset(right, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            LayerActionIcon.REMOVE -> {
                drawLine(
                    color = color,
                    start = Offset(left, top),
                    end = Offset(right, bottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(right, top),
                    end = Offset(left, bottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun RulePicker(selectedRule: String, onSelect: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedPreset = RulePreset.entries.firstOrNull { preset -> preset.rule == selectedRule }
    val selectedName = selectedPreset?.displayName() ?: selectedRule

    EinkDisclosureRow(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        Text(
            text = stringResource(R.string.rule_selected, selectedName),
            modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
            fontWeight = FontWeight.Bold,
        )
    }
    if (isExpanded) {
        OptionRow(
            label = null,
            options = RulePreset.entries.map { preset -> preset.displayName() to preset.rule },
            selected = selectedRule,
            onSelect = onSelect,
        )
    }
}
