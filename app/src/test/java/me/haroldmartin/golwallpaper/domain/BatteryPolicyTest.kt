@file:Suppress("IllegalIdentifier")

package me.haroldmartin.golwallpaper.domain

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatteryPolicyTest {
    @Test
    fun `threshold of zero never skips`() {
        assertFalse(isBelowBatteryThreshold(batteryLevelPct = 5, thresholdPct = 0))
        assertFalse(isBelowBatteryThreshold(batteryLevelPct = 0, thresholdPct = 0))
    }

    @Test
    fun `negative threshold never skips`() {
        assertFalse(isBelowBatteryThreshold(batteryLevelPct = 5, thresholdPct = -10))
    }

    @Test
    fun `level below threshold skips`() {
        assertTrue(isBelowBatteryThreshold(batteryLevelPct = 9, thresholdPct = 20))
        assertTrue(isBelowBatteryThreshold(batteryLevelPct = 0, thresholdPct = 20))
    }

    @Test
    fun `level at threshold does not skip`() {
        assertFalse(isBelowBatteryThreshold(batteryLevelPct = 20, thresholdPct = 20))
    }

    @Test
    fun `level above threshold does not skip`() {
        assertFalse(isBelowBatteryThreshold(batteryLevelPct = 80, thresholdPct = 20))
    }

    @Test
    fun `unknown level does not skip`() {
        assertFalse(isBelowBatteryThreshold(batteryLevelPct = -1, thresholdPct = 20))
    }
}
