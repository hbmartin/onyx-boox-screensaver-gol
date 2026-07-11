@file:Suppress("IllegalIdentifier")

package me.haroldmartin.golwallpaper.domain

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReseedPolicyTest {
    @Test
    fun `reseeds when the board is dead`() {
        assertTrue(
            shouldReseed(
                population = 0,
                currentState = "state",
                prevState = "other",
                prevPrevState = "another",
            ),
            "A board with no live cells should reseed",
        )
    }

    @Test
    fun `reseeds when current equals the previous generation (still life)`() {
        assertTrue(
            shouldReseed(
                population = 4,
                currentState = "block",
                prevState = "block",
                prevPrevState = "something",
            ),
            "A still life (equal to the previous generation) should reseed",
        )
    }

    @Test
    fun `reseeds when current equals two generations back (period-2 oscillator)`() {
        assertTrue(
            shouldReseed(
                population = 3,
                currentState = "blinkerA",
                prevState = "blinkerB",
                prevPrevState = "blinkerA",
            ),
            "A period-2 oscillator (equal to two generations back) should reseed",
        )
    }

    @Test
    fun `does not reseed a living, changing board`() {
        assertFalse(
            shouldReseed(
                population = 12,
                currentState = "gen3",
                prevState = "gen2",
                prevPrevState = "gen1",
            ),
            "A board that keeps changing should not reseed",
        )
    }

    @Test
    fun `null previous states never match a live board`() {
        assertFalse(
            shouldReseed(
                population = 5,
                currentState = "gen1",
                prevState = null,
                prevPrevState = null,
            ),
            "Missing history should not trigger a reseed on a live board",
        )
    }
}
