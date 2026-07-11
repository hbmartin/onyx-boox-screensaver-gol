package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.SaveLayers

class SaveLayersImpl(private val dataStore: UserDataStore) : SaveLayers {
    @Suppress("NoCallbacksInFunctions")
    override suspend fun mutate(transform: (List<Layer>) -> List<Layer>) {
        dataStore.update(UserDataStore.Keys.LAYERS) { storedLayers ->
            LayersSerializer.encode(transform(LayersSerializer.decode(storedLayers)))
        }
    }
}
