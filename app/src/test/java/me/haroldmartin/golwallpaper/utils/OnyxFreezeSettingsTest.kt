package me.haroldmartin.golwallpaper.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnyxFreezeSettingsTest {
    private val packageName = "me.haroldmartin.golwallpaper"

    @Test
    fun `security failure launches fallback`() {
        var didLaunchFallback = false

        launchWithFallback(
            launch = { throw SecurityException("Vendor activity is not exported") },
            fallback = { didLaunchFallback = true },
        )

        assertTrue(didLaunchFallback)
    }

    @Test
    fun `successful launch does not launch fallback`() {
        var didLaunch = false
        var didLaunchFallback = false

        launchWithFallback(
            launch = { didLaunch = true },
            fallback = { didLaunchFallback = true },
        )

        assertTrue(didLaunch)
        assertFalse(didLaunchFallback)
    }

    @Test
    fun `unrelated launch failure is not swallowed`() {
        var didLaunchFallback = false

        assertFailsWith<IllegalStateException> {
            launchWithFallback(
                launch = { throw IllegalStateException("Unexpected launch failure") },
                fallback = { didLaunchFallback = true },
            )
        }

        assertFalse(didLaunchFallback)
    }

    @Test
    fun `collection reports package as auto frozen`() {
        assertTrue(isPackageAutoFrozen(listOf(packageName), packageName) == true)
    }

    @Test
    fun `collection reports package as not auto frozen`() {
        assertEquals(false, isPackageAutoFrozen(listOf("another.package"), packageName))
    }

    @Test
    fun `array results are supported`() {
        assertTrue(isPackageAutoFrozen(arrayOf(packageName), packageName) == true)
    }

    @Test
    fun `unknown result keeps detection inconclusive`() {
        assertNull(isPackageAutoFrozen("unexpected", packageName))
    }

    @Test
    fun `unknown collection entries keep detection inconclusive`() {
        assertNull(isPackageAutoFrozen(listOf(Any()), packageName))
    }
}
