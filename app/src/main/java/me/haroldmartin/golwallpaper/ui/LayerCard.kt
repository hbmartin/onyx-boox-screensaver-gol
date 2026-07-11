package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.RulePreset
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM

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
                ColorPicker(
                    label = stringResource(R.string.fg_color),
                    selectedColor = layer.fgColor,
                    onClick = callbacks.onColorChange,
                )
                OptionRow(
                    label = stringResource(R.string.rule_label),
                    options = RulePreset.entries.map { preset -> preset.displayName() to preset.rule },
                    selected = layer.rule,
                    onSelect = callbacks.onRuleChange,
                )
                PatternPicker(onClick = callbacks.onResetPattern)
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
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = callbacks.onEnabledChange,
            modifier = Modifier.semantics {
                contentDescription = enableDescription
            },
        )
        LayerIconButton(
            text = "↑",
            description = stringResource(R.string.move_layer_up),
            enabled = index > 0,
            onClick = callbacks.onMoveUp,
        )
        LayerIconButton(
            text = "↓",
            description = stringResource(R.string.move_layer_down),
            enabled = index < layerCount - 1,
            onClick = callbacks.onMoveDown,
        )
        LayerIconButton(
            text = "×",
            description = stringResource(R.string.delete_layer),
            enabled = layerCount > 1,
            onClick = callbacks.onDelete,
        )
    }
}

@Composable
private fun LayerIconButton(
    text: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Text(text)
    }
}
