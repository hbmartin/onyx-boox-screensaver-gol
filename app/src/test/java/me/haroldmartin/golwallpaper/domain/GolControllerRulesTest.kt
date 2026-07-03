@file:Suppress("IllegalIdentifier")

package me.haroldmartin.golwallpaper.domain

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GolControllerRulesTest {
    private fun assertGridMatches(
        expected: List<String>,
        actual: Array<BooleanArray>,
        message: String,
    ) {
        val actualStrings = actual.map { row ->
            row.joinToString("") { if (it) "A" else "." }
        }
        assertEquals(expected, actualStrings, message)
    }

    @Test
    fun `Seeds rule - B2 S - births on 2 neighbours and everything dies`() {
        // Two adjacent cells centered on 5x5: (2,1) and (2,2).
        val controller = GolController(5, 5, initialPattern = "AA", rule = "B2/S")
        controller.update()
        assertGridMatches(
            expected = listOf(
                ".....",
                ".AA..",
                ".....",
                ".AA..",
                ".....",
            ),
            actual = controller.grid,
            message = "Seeds: dead cells with 2 neighbours born, live cells always die",
        )
    }

    @Test
    fun `Life without Death rule - live cells never die`() {
        // Vertical blinker centered on 5x5.
        val controller =
            GolController(5, 5, initialPattern = ".A.$.A.$.A.", rule = "B3/S012345678")
        controller.update()
        assertGridMatches(
            expected = listOf(
                ".....",
                "..A..",
                ".AAA.",
                "..A..",
                ".....",
            ),
            actual = controller.grid,
            message = "LwoD: blinker grows into a plus instead of oscillating",
        )
    }

    @Test
    fun `HighLife rule - dead cell with 6 neighbours is born`() {
        // Center cell (2,2) of the 5x5 grid has exactly 6 live neighbours.
        val pattern = "AAA\$A.A\$A.."
        val conway = GolController(5, 5, initialPattern = pattern, rule = "B3/S23")
        val highLife = GolController(5, 5, initialPattern = pattern, rule = "B36/S23")

        conway.update()
        highLife.update()

        assertFalse(conway[2 to 2], "Conway: 6 neighbours must not give birth")
        assertTrue(highLife[2 to 2], "HighLife: 6 neighbours must give birth")
    }

    @Test
    fun `population counts live cells`() {
        val controller = GolController(4, 4, initialPattern = "AA$.A")
        assertEquals(3, controller.population, "Initial population")

        controller.reset("....")
        assertEquals(0, controller.population, "Population after clearing")
    }

    @Test
    fun `blinker oscillates back to initial state after two updates`() {
        val controller = GolController(5, 5, initialPattern = ".A.$.A.$.A.")
        val initial = controller.grid.map { it.clone() }.toTypedArray()

        controller.update()
        val afterOne = controller.grid.map { row ->
            row.joinToString("") { if (it) "A" else "." }
        }
        assertEquals(
            listOf(".....", ".....", ".AAA.", ".....", "....."),
            afterOne,
            "Blinker phase 2 should be horizontal",
        )

        controller.update()
        initial.forEachIndexed { r, expectedRow ->
            expectedRow.forEachIndexed { c, expectedCell ->
                assertEquals(
                    expectedCell,
                    controller.grid[r][c],
                    "Blinker should return to phase 1 at ($r, $c)",
                )
            }
        }
    }

    @Test
    fun `many updates keep grid dimensions stable`() {
        val controller = GolController(6, 7, initialPattern = "bob\$2bo\$3o!")
        repeat(50) {
            val grid = controller.update()
            assertEquals(6, grid.size, "Row count after update")
            assertTrue(grid.all { it.size == 7 }, "Column count after update")
        }
    }

    @Test
    fun `toString round trips through the parser`() {
        val controller = GolController(6, 7, initialPattern = "bob\$2bo\$3o!")
        controller.update()
        val serialized = controller.toString()

        val restored = GolController(6, 7, initialPattern = serialized)
        controller.grid.forEachIndexed { r, expectedRow ->
            expectedRow.forEachIndexed { c, expectedCell ->
                assertEquals(
                    expectedCell,
                    restored.grid[r][c],
                    "Round-tripped cell ($r, $c) differs",
                )
            }
        }
    }

    @Test
    fun `reset after update uses fresh buffers`() {
        val controller = GolController(5, 5, initialPattern = ".A.$.A.$.A.")
        controller.update()
        controller.reset("AA")
        assertEquals(2, controller.population, "Population after reset")
        controller.update()
        assertEquals(0, controller.population, "Two lonely cells die after reset + update")
    }
}
