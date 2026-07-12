package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.Layer
import org.json.JSONArray
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayersSerializerTest {
    @Test
    fun `encode then decode preserves every field including id`() {
        val layers = listOf(
            Layer(
                fgColor = 1,
                rule = "B3/S23",
                state = "abc",
                startingPattern = "ARK1",
                generation = 4,
                isEnabled = true,
            ),
            Layer(fgColor = 2, rule = "B36/S23", state = null, generation = 0, isEnabled = false),
        )

        val decoded = LayersSerializer.decode(LayersSerializer.encode(layers))

        assertEquals(layers, decoded)
    }

    @Test
    fun `decode of legacy layers without id assigns distinct non-blank ids`() {
        val legacy = JSONArray().apply {
            put(JSONObject().put("fg", 1).put("rule", "B3/S23"))
            put(JSONObject().put("fg", 2).put("rule", "B3/S23"))
        }.toString()

        val decoded = LayersSerializer.decode(legacy)

        assertEquals(2, decoded.size)
        decoded.forEach { layer -> assertTrue(layer.id.isNotBlank()) }
        // Distinct ids are required so Compose `key(layer.id)` does not collide.
        assertEquals(2, decoded.map(Layer::id).toSet().size)
    }

    @Test
    fun `decode of malformed json falls back to a single default layer`() {
        val decoded = LayersSerializer.decode("not json at all")

        assertEquals(listOf(Layer()).map { it.copy(id = decoded.single().id) }, decoded)
    }

    @Test
    fun `decode of blank input falls back to a single default layer`() {
        assertEquals(1, LayersSerializer.decode("").size)
        assertEquals(1, LayersSerializer.decode(null).size)
    }
}
