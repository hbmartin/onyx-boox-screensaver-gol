package me.haroldmartin.golwallpaper.domain

import androidx.compose.ui.graphics.toArgb
import me.haroldmartin.golwallpaper.ui.theme.Colors

object LayerOps {
    fun addDefault(layers: List<Layer>, nextColor: Int = nextUnusedColor(layers)): List<Layer> =
        layers + Layer(fgColor = nextColor)

    fun removeAt(layers: List<Layer>, index: Int): List<Layer> =
        if (layers.size <= 1 || index !in layers.indices) {
            layers
        } else {
            layers.filterIndexed { layerIndex, _ -> layerIndex != index }
        }

    fun moveUp(layers: List<Layer>, index: Int): List<Layer> = swap(layers, index, index - 1)

    fun moveDown(layers: List<Layer>, index: Int): List<Layer> = swap(layers, index, index + 1)

    fun setEnabled(layers: List<Layer>, index: Int, isEnabled: Boolean): List<Layer> =
        updateAt(layers, index) { layer -> layer.copy(isEnabled = isEnabled) }

    @Suppress("NoCallbacksInFunctions")
    fun updateAt(
        layers: List<Layer>,
        index: Int,
        transform: (Layer) -> Layer,
    ): List<Layer> = layers.mapIndexed { layerIndex, layer ->
        if (layerIndex == index) transform(layer) else layer
    }

    fun nextUnusedColor(layers: List<Layer>): Int {
        val usedColors = layers.map(Layer::fgColor).toSet()
        return Colors.ALL
            .map { color -> color.value.toArgb() }
            .firstOrNull { color -> color !in usedColors }
            ?: Colors.ALL[layers.size % Colors.ALL.size].value.toArgb()
    }

    private fun swap(layers: List<Layer>, from: Int, to: Int): List<Layer> =
        if (from !in layers.indices || to !in layers.indices) {
            layers
        } else {
            layers.mapIndexed { index, layer ->
                when (index) {
                    from -> layers[to]
                    to -> layers[from]
                    else -> layer
                }
            }
        }
}
