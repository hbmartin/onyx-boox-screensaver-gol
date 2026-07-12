package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.RulePreset
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.LARGE
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM

private val LAYER_ACTION_BUTTON_SIZE = 56.dp
private val LAYER_ACTION_ICON_SIZE = 32.dp
private val LAYER_ACTION_STROKE_WIDTH = 4.dp
private const val DISABLED_ICON_ALPHA = 0.65f

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(MEDIUM),
            verticalArrangement = Arrangement.spacedBy(MEDIUM),
        ) {
            LayerHeader(
                index = index,
                layerCount = layerCount,
                isExpanded = isExpanded,
                isEnabled = layer.isEnabled,
                callbacks = callbacks,
                onToggleExpansion = { isExpanded = !isExpanded },
            )
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(start = LARGE),
                    verticalArrangement = Arrangement.spacedBy(MEDIUM),
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
@Suppress("LongParameterList")
private fun LayerHeader(
    index: Int,
    layerCount: Int,
    isExpanded: Boolean,
    isEnabled: Boolean,
    callbacks: LayerCallbacks,
    onToggleExpansion: () -> Unit,
) {
    val enableDescription = stringResource(R.string.enable_layer)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MEDIUM),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MEDIUM),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggleExpansion),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Disclosure(isExpanded)
                Text(
                    text = stringResource(R.string.layer_number, index + 1),
                    modifier = Modifier.padding(horizontal = MEDIUM),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = callbacks.onEnabledChange,
                modifier = Modifier.semantics {
                    contentDescription = enableDescription
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
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
}

@Composable
private fun LayerIconButton(
    icon: LayerActionIcon,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(LAYER_ACTION_BUTTON_SIZE)
            .semantics { contentDescription = description },
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = colorScheme.onSurface,
            disabledContentColor = colorScheme.onSurface.copy(alpha = DISABLED_ICON_ALPHA),
        ),
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

    Row(
        modifier = Modifier.clickable { isExpanded = !isExpanded },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Disclosure(isExpanded)
        Text(
            text = stringResource(R.string.rule_selected, selectedName),
            modifier = Modifier.padding(horizontal = MEDIUM),
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
