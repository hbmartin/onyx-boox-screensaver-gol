package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import me.haroldmartin.einkui.EinkButton
import me.haroldmartin.einkui.EinkTheme
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.Layer

private const val SOFT_LAYER_CAP = 10

@Composable
fun LayersSection(
    layers: List<Layer>,
    callbacks: LayersCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
    ) {
        Text(stringResource(R.string.layers_title))
        layers.forEachIndexed { index, layer ->
            key(layer.id) {
                LayerCard(
                    layer = layer,
                    index = index,
                    layerCount = layers.size,
                    callbacks = LayerCallbacks(
                        onMoveUp = { callbacks.onMoveUp(index) },
                        onMoveDown = { callbacks.onMoveDown(index) },
                        onDelete = { callbacks.onRemove(index) },
                        onColorChange = { color -> callbacks.onColorChange(index, color) },
                        onRuleChange = { rule -> callbacks.onRuleChange(index, rule) },
                        onResetPattern = { pattern, startingPattern ->
                            callbacks.onResetPattern(index, pattern, startingPattern)
                        },
                    ),
                )
            }
        }
        if (layers.size >= SOFT_LAYER_CAP) {
            Text(pluralStringResource(R.plurals.max_layers_warning, SOFT_LAYER_CAP, SOFT_LAYER_CAP))
        }
        EinkButton(onClick = callbacks.onAdd) {
            Text(stringResource(R.string.add_layer))
        }
    }
}
