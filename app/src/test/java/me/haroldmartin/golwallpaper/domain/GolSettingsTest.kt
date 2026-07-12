package me.haroldmartin.golwallpaper.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class GolSettingsTest {
    @Test
    fun `output orientation parses every stored value`() {
        OutputOrientation.entries.forEach { orientation ->
            assertEquals(orientation, OutputOrientation.fromString(orientation.name))
        }
    }

    @Test
    fun `output orientation defaults to auto for missing or unknown values`() {
        assertEquals(OutputOrientation.AUTO, OutputOrientation.fromString(null))
        assertEquals(OutputOrientation.AUTO, OutputOrientation.fromString("SIDEWAYS"))
    }
}
