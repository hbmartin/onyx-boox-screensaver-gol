package me.haroldmartin.golwallpaper.domain

interface SaveLayers {
    @Suppress("NoCallbacksInFunctions")
    suspend fun mutate(transform: (List<Layer>) -> List<Layer>)
}

suspend fun SaveLayers.addLayer() = mutate(LayerOps::addDefault)

suspend fun SaveLayers.removeLayer(index: Int) = mutate { layers -> LayerOps.removeAt(layers, index) }

suspend fun SaveLayers.moveUp(index: Int) = mutate { layers -> LayerOps.moveUp(layers, index) }

suspend fun SaveLayers.moveDown(index: Int) = mutate { layers -> LayerOps.moveDown(layers, index) }

suspend fun SaveLayers.setEnabled(index: Int, isEnabled: Boolean) = mutate { layers ->
    LayerOps.setEnabled(layers, index, isEnabled)
}

suspend fun SaveLayers.setFgColor(index: Int, color: Int) = mutate { layers ->
    LayerOps.updateAt(layers, index) { layer -> layer.copy(fgColor = color) }
}

suspend fun SaveLayers.setRule(index: Int, rule: String) = mutate { layers ->
    LayerOps.updateAt(layers, index) { layer -> layer.copy(rule = rule) }
}

suspend fun SaveLayers.resetPattern(
    index: Int,
    pattern: String?,
    startingPattern: String?,
) = mutate { layers ->
    LayerOps.updateAt(layers, index) { layer ->
        layer.copy(
            state = pattern,
            startingPattern = startingPattern,
            generation = 0,
        )
    }
}

suspend fun SaveLayers.writeStates(newLayers: List<Layer>) = mutate { newLayers }
