package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.DEFAULT_FG
import me.haroldmartin.golwallpaper.domain.DEFAULT_RULE
import me.haroldmartin.golwallpaper.domain.Layer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

object LayersSerializer {
    private const val ID = "id"
    private const val FG = "fg"
    private const val RULE = "rule"
    private const val STATE = "state"
    private const val GENERATION = "gen"
    private const val ENABLED = "enabled"

    fun encode(layers: List<Layer>): String {
        val array = JSONArray()
        layers.forEach { layer ->
            array.put(
                JSONObject()
                    .put(ID, layer.id)
                    .put(FG, layer.fgColor)
                    .put(RULE, layer.rule)
                    .put(STATE, layer.state ?: JSONObject.NULL)
                    .put(GENERATION, layer.generation)
                    .put(ENABLED, layer.isEnabled),
            )
        }
        return array.toString()
    }

    @Suppress("SwallowedException")
    fun decode(value: String?): List<Layer> = if (value.isNullOrBlank()) {
        defaultLayers()
    } else {
        try {
            val array = JSONArray(value)
            List(array.length()) { index -> array.getJSONObject(index).toLayer() }
                .ifEmpty(::defaultLayers)
        } catch (exception: JSONException) {
            defaultLayers()
        }
    }

    private fun JSONObject.toLayer(): Layer = Layer(
        fgColor = optInt(FG, DEFAULT_FG),
        rule = optString(RULE, DEFAULT_RULE),
        state = if (isNull(STATE)) null else optString(STATE),
        generation = optInt(GENERATION, 0).coerceAtLeast(0),
        isEnabled = optBoolean(ENABLED, true),
        id = optString(ID).ifEmpty { UUID.randomUUID().toString() },
    )

    private fun defaultLayers(): List<Layer> = listOf(Layer())
}
