@file:Suppress("IllegalIdentifier")

package me.haroldmartin.golwallpaper.domain

import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GolControllerTest {
    //region Tests Setup
    private val standardRule = "B3/S23"

    /**
     * Creates a grid from a list of strings, where 'A' or 'X' denotes a live cell
     * and '.' denotes a dead cell.
     * The input pattern strings should represent the entire desired grid state.
     */
    private fun createGridFromArrayStrings(pattern: List<String>): Array<BooleanArray> {
        if (pattern.isEmpty()) return emptyArray()
        val rows = pattern.size
        val cols = pattern[0].length
        return Array(rows) { r ->
            BooleanArray(cols) { c ->
                pattern[r][c] == 'A' || pattern[r][c] == 'X'
            }
        }
    }

    /**
     * Asserts that two grids are equal cell by cell.
     */
    private fun assertGridsEqual(
        expected: Array<BooleanArray>,
        actual: Array<BooleanArray>,
        message: String? = null,
    ) {
        val prefix = message?.let { "$it: " } ?: ""
        assertEquals(expected.size, actual.size, "${prefix}Grid row counts differ.")
        expected.forEachIndexed { r, expectedRow ->
            assertTrue(actual.size > r, "${prefix}Actual grid has fewer rows than expected.")
            assertEquals(
                expectedRow.size,
                actual[r].size,
                "${prefix}Grid column counts differ for row $r.",
            )
            expectedRow.forEachIndexed { c, expectedCell ->
                assertEquals(
                    expectedCell,
                    actual[r][c],
                    "${prefix}Cell ($r, $c) differs.\nExpected:\n${
                        gridToString(
                            expected,
                        )
                    }\nActual:\n${gridToString(actual)}",
                )
            }
        }
    }

    /**
     * Converts a grid to a simple string representation for debugging.
     * 'A' for live, '.' for dead.
     */
    private fun gridToString(grid: Array<BooleanArray>): String = grid.joinToString("\n") { row ->
        row.joinToString("") { if (it) "A" else "." }
    }

    //endregion

    //region Initial Grid Creation
    @Test
    fun `Create Empty Grid - using empty pattern string`() {
        // The controller centers the pattern. An "empty" pattern string like "..." on a 3x3 grid.
        val controller = GolController(3, 3, initialPattern = "...\$...\$...", rule = standardRule)
        val expectedGrid = createGridFromArrayStrings(
            listOf(
                "...",
                "...",
                "...",
            ),
        )
        assertGridsEqual(expectedGrid, controller.grid, "T1.1 Empty Grid")
    }

    @Test
    fun `Create Grid with Initial Pattern - string pattern`() {
        val rows = 3
        val cols = 3
        // Pattern "A" will be centered.
        // (3-1)/2 = 1. So grid[1][1] should be alive.
        val controller = GolController(rows, cols, initialPattern = "A", rule = standardRule)
        val expectedGrid = createGridFromArrayStrings(
            listOf(
                "...",
                ".A.",
                "...",
            ),
        )
        assertGridsEqual(expectedGrid, controller.grid, "T1.2 Initial Pattern 'A'")

        // More complex pattern
        val controller2 = GolController(4, 4, initialPattern = "AA$.A", rule = standardRule)
        // Pattern: AA  (2x2, height 2, width 2)
        //          .A
        // Centered on 4x4: startRow = (4-2)/2 = 1, startCol = (4-2)/2 = 1
        val expectedGrid2 = createGridFromArrayStrings(
            listOf(
                "....",
                ".AA.",
                "..A.",
                "....",
            ),
        )
        assertGridsEqual(expectedGrid2, controller2.grid, "T1.2 Initial Pattern 'AA\$.A'")
    }

    @Test
    fun `Create Grid with Invalid Dimensions - zero or negative`() {
        assertThrows("T1.3 Zero columns", IllegalArgumentException::class.java) {
            GolController(5, 0, initialPattern = "A", rule = standardRule)
        }
        assertThrows("T1.3 Zero rows", IllegalArgumentException::class.java) {
            GolController(0, 5, initialPattern = "A", rule = standardRule)
        }
        assertThrows("T1.3 Negative columns", IllegalArgumentException::class.java) {
            GolController(5, -1, initialPattern = "A", rule = standardRule)
        }
        // Test invalid rule string format (size)
        assertThrows("T1.3 Invalid birth rule size", IllegalArgumentException::class.java) {
            GolController(
                3,
                3,
                birthRule = BooleanArray(5),
                surviveRule = BooleanArray(9),
                initialPattern = null,
            )
        }
        assertThrows("T1.3 Invalid survive rule size", IllegalArgumentException::class.java) {
            GolController(
                3,
                3,
                birthRule = BooleanArray(9),
                surviveRule = BooleanArray(2),
                initialPattern = null,
            )
        }
        assertThrows("T1.3 Pattern too tall", IllegalArgumentException::class.java) {
            GolController(2, 5, initialPattern = "A\$A\$A") // 3 rows high pattern
        }
        assertThrows("T1.3 Pattern too wide", IllegalArgumentException::class.java) {
            GolController(5, 2, initialPattern = "AAA") // 3 cols wide pattern
        }
    }

    @Test
    fun `Get Cell State`() {
        val controller = GolController(3, 3, initialPattern = ".A.\$A.A$.A.", rule = standardRule)
        // Centered Tub on 3x3:
        // .A.
        // A.A
        // .A.
        assertTrue(controller[0 to 1], "T1.4 Tub (0,1)")
        assertFalse(controller[0 to 0], "T1.4 Tub (0,0)")
        assertTrue(controller[1 to 0], "T1.4 Tub (1,0)")
        assertFalse(controller[1 to 1], "T1.4 Tub (1,1)")
    }

    @Test
    fun `Get Cell State - Out of Bounds`() {
        val controller = GolController(3, 3, initialPattern = "A", rule = standardRule)
        assertThrows("T1.5 Get (-1,0)", ArrayIndexOutOfBoundsException::class.java) {
            controller[-1 to 0]
        }
        assertThrows("T1.5 Get (3,0)", ArrayIndexOutOfBoundsException::class.java) {
            controller[3 to 0]
        }
    }

    @Test
    fun `Set Cell State - turnOnCell`() {
        val controller = GolController(3, 3, initialPattern = "...", rule = standardRule)
        assertFalse(controller[1 to 1], "T1.6 Initial state (1,1) before turnOnCell")
        controller.turnOnCell(1, 1)
        assertTrue(controller[1 to 1], "T1.6 State (1,1) after turnOnCell")
    }

    @Test
    fun `Set Cell State - Out of Bounds - turnOnCell`() {
        val controller = GolController(3, 3, initialPattern = "...", rule = standardRule)
        assertThrows("T1.7 Set (-1,0)", ArrayIndexOutOfBoundsException::class.java) {
            controller.turnOnCell(-1, 0)
        }
        assertThrows("T1.7 Set (3,0)", ArrayIndexOutOfBoundsException::class.java) {
            controller.turnOnCell(3, 0)
        }
    }

    //endregion

    //region Core Logic - Cell State Transitions (Single Tick)

    @Test
    fun `Underpopulation - 0 or 1 neighbour`() {
        // Cell with 0 neighbours
        var controller = GolController(3, 3, initialPattern = "A", rule = standardRule)
        // . . .
        // . A .
        // . . .
        controller.update()
        assertFalse(controller[1 to 1], "T2.1 Cell with 0 neighbours should die")

        // Cell with 1 neighbour
        controller = GolController(3, 3, initialPattern = "AA", rule = standardRule) // Centered AA
        // . . .
        // .AA .  (original pattern is AA, width 2, height 1.
        // Centered on 3x3: (3-1)/2=1, (3-2)/2=0 -> row 1, col 0 and 1)
        // . . .   Actually, (3-1)/2=1, (3-2)/2=0.  startRow=1, startCol=0. grid[1][0], grid[1][1]
        // This means the initial pattern "AA" on 3x3 is:
        // ...
        // AA.
        // ...
        // Cell (1,0) has 1 neighbour (1,1). Cell (1,1) has 1 neighbour (1,0). Both should die.
        controller.update()
        assertFalse(controller[1 to 0], "T2.1 Cell (1,0) with 1 neighbour should die")
        assertFalse(controller[1 to 1], "T2.1 Cell (1,1) with 1 neighbour should die")
    }

    @Test
    fun `Overpopulation - more than 3 neighbours`() {
        // Live cell with 4 neighbours
        // AAA
        // A A  <- center A is the target, grid[1][1]
        // AAA
        // Pattern: "AAA\$A.A\$AAA" (target cell is initially dead, this is for reproduction)
        // Let's use a 3x3 all live cells, center cell (1,1) has 8 neighbours.
        // AAA
        // AAA
        // AAA
        var controller = GolController(3, 3, initialPattern = "AAA\$AAA\$AAA", rule = standardRule)
        assertTrue(controller[1 to 1], "T2.3 Center cell initially live")
        controller.update() // Center cell (1,1) had 8 neighbours, should die.
        // Expected:
        // A.A
        // ...
        // A.A
        assertFalse(controller[1 to 1], "T2.3 Cell with 8 neighbours should die")

        // Live cell with 4 neighbours:
        // .A.
        // AAA
        // .A.
        // Cell (1,1) has 4 neighbours.
        controller = GolController(3, 3, initialPattern = ".A.\$AAA$.A.", rule = standardRule)
        // Centered on 3x3:
        // .A.
        // AAA
        // .A.
        assertTrue(controller[1 to 1], "T2.3 Cell (1,1) initially live with 4 neighbours")
        controller.update()
        assertFalse(controller.grid[1][1], "T2.3 Cell with 4 neighbours should die")
    }

    @Test
    fun `Reproduction - dead cell with exactly 3 neighbours`() {
        // Center of a horizontal blinker line of 3
        // AAA -> becomes vertical A,A,A. The center cell (1,1) was dead.
        // Initial (for reproduction test):
        // A.A
        // . .  <- target cell (1,1) is dead
        // A.A
        // Cell (1,1) has 4 neighbours. Not good for this test.

        // Use Blinker transition:
        // Phase 1 (Vertical):  Phase 2 (Horizontal):
        // .A.                   ...
        // .A.         --->      AAA
        // .A.                   ...
        // Consider cell (1,0) in Phase 2. It was dead. Neighbours in Phase 1:
        // (0,1), (1,1), (2,1) (all live). So 3 live neighbours. It should become live.
        val controller = GolController(3, 3, initialPattern = ".A.$.A.$.A.", rule = standardRule)
        assertFalse(controller[1 to 0], "T2.4 Cell (1,0) initially dead")
        controller.update()
        assertTrue(controller[1 to 0], "T2.4 Dead cell (1,0) with 3 neighbours should become live")
    }

    @Test
    fun `Dead Cell Stays Dead - not 3 neighbours`() {
        // Dead cell with 0 neighbours
        var controller = GolController(3, 3, initialPattern = "...", rule = standardRule)
        assertFalse(controller[1 to 1], "T2.5 Cell (1,1) initially dead")
        controller.update()
        assertFalse(controller[1 to 1], "T2.5 Dead cell with 0 neighbours stays dead")

        // Dead cell with 2 neighbours
        // A.A
        // . .  <- (1,1) is dead, has 2 neighbours (0,1) and (2,1) if pattern is ".A.$...$.A."
        // . .
        controller = GolController(3, 3, initialPattern = ".A.$...$.A.", rule = standardRule)
        // .A.
        // ...
        // .A.
        // Cell (0,0) is dead. Neighbours (toroidal): (2,2), (2,0), (2,1), (0,2), (0,1), (1,2), (1,0), (1,1)
        // (0,1) is live, (2,1) is live. So (0,0) has 2 live neighbours.
        assertFalse(controller[0 to 0], "T2.5 Cell (0,0) initially dead")
        controller.update()
        assertFalse(controller[0 to 0], "T2.5 Dead cell with 2 neighbours stays dead")

        // Dead cell with 4 neighbours
        // AAA
        // A.A <- (1,1) is dead, has 8 neighbours.
        // AAA
        controller = GolController(3, 3, initialPattern = "AAA\$A.A\$AAA", rule = standardRule)
        assertFalse(controller[1 to 1], "T2.5 Cell (1,1) initially dead")
        controller.update() // (1,1) has 8 live neighbours, should stay dead by B3 rule.
        assertFalse(controller[1 to 1], "T2.5 Dead cell with 8 neighbours stays dead")
    }

    //endregion

    //region Boundary Conditions and Grid Topology (Toroidal)

    @Test
    fun `Toroidal Boundaries - Corner Cell Evolution`() {
        // Live cell at (0,0) with 2 live neighbours wrapping around.
        // Grid 3x3. Live cells: (0,0), (0,2), (2,0)
        // A . A
        // . . .
        // A . .
        // Cell (0,0) has neighbours: (2,2)(dead), (2,0)(live), (2,1)(dead),
        //                           (0,2)(live), (0,1)(dead),
        //                           (1,2)(dead), (1,0)(dead), (1,1)(dead)
        // So, (0,0) has 2 live neighbours: (0,2) and (2,0). It should survive.
        val controller = GolController(3, 3, initialPattern = "A.A$...\$A..", rule = standardRule)
        assertTrue(controller[0 to 0], "T4.3 Toroidal: Cell (0,0) initially live")
        controller.update()
        assertTrue(
            controller[0 to 0],
            "T4.3 Toroidal: Cell (0,0) with 2 wrapped neighbours should survive",
        )

        // Cell (0,0) with 3 live neighbours wrapping around for birth
        // . . A  (0,2)
        // A . .  (1,0)
        // . A .  (2,1)
        // Target (0,0) is dead. Neighbours of (0,0):
        // (2,2) (.), (2,0) (.), (2,1) (A) <- 1
        // (0,2) (A), (0,1) (.),           <- 1
        // (1,2) (.), (1,0) (A), (1,1) (.) <- 1
        // Total 3 live neighbours. (0,0) should be born.
        val birthController =
            GolController(3, 3, initialPattern = "..A\$A..$.A.", rule = standardRule)
        assertFalse(
            birthController[0 to 0],
            "T4.3 Toroidal: Cell (0,0) initially dead for birth test",
        )
        birthController.update()
        assertTrue(
            birthController[0 to 0],
            "T4.3 Toroidal: Dead cell (0,0) with 3 wrapped neighbours should be born",
        )
    }

    @Test
    fun `Toroidal Boundaries - Edge Cell Evolution`() {
        // Live cell at (0,1) (edge, not corner) on 3x3 grid.
        // Neighbours: (2,0),(2,1),(2,2), (0,0),(0,2), (1,0),(1,1),(1,2)
        // Pattern:
        // .A.  (0,1) is live
        // .A.  (1,1) is live (neighbour)
        // A..  (2,0) is live (neighbour)
        // Cell (0,1) has neighbours: (2,0)(A), (2,1)(.), (2,2)(.), (0,0)(.), (0,2)(.), (1,0)(.), (1,1)(A), (1,2)(.)
        // So, (0,1) has 2 live neighbours: (2,0) and (1,1). It should survive.
        val controller = GolController(3, 3, initialPattern = ".A.$.A.\$A..", rule = standardRule)
        assertTrue(controller[0 to 1], "T4.4 Toroidal: Cell (0,1) initially live")
        controller.update()
        assertTrue(
            controller[0 to 1],
            "T4.4 Toroidal: Cell (0,1) with 2 wrapped/local neighbours should survive",
        )
    }

    @Test
    fun `Single Cell Grid`() {
        // Live cell
        var controller = GolController(1, 1, initialPattern = "A", rule = standardRule)
        controller.update()
        assertFalse(controller[0 to 0], "T4.6 Single live cell dies")

        // Dead cell
        controller = GolController(1, 1, initialPattern = ".", rule = standardRule)
        controller.update()
        assertFalse(controller[0 to 0], "T4.6 Single dead cell stays dead")
    }

    //endregion

    //region Still Lifes (Stable Patterns)

    @Test
    fun `Still Life - Tub`() {
        // .A.
        // A.A
        // .A.
        // Pattern: ".A.\$A.A$.A." (3x3)
        val controller = GolController(3, 3, initialPattern = ".A.\$A.A$.A.", rule = standardRule)
        val initialGrid = controller.grid.map { it.clone() }.toTypedArray()
        controller.update()
        assertGridsEqual(initialGrid, controller.grid, "T5.5 Tub should be stable")
    }

    //endregion

    //region Edge Cases and Specific Scenarios

    @Test
    fun `All Dead Grid`() {
        val controller = GolController(3, 3, initialPattern = "...", rule = standardRule)
        controller.update()
        val expectedGrid = createGridFromArrayStrings(listOf("...", "...", "..."))
        assertGridsEqual(expectedGrid, controller.grid, "T8.1 All dead grid remains all dead")
    }

    @Test
    fun `Death of a Pattern - single cell`() {
        val controller = GolController(3, 3, initialPattern = "A", rule = standardRule)
        controller.update()
        val expectedGrid = createGridFromArrayStrings(listOf("...", "...", "..."))
        assertGridsEqual(expectedGrid, controller.grid, "T8.3 Single live cell dies")
    }

    @Test
    fun `Death of a Pattern - two adjacent cells`() {
        val controller = GolController(3, 3, initialPattern = "AA", rule = standardRule)
        controller.update() // Both die in 1 tick
        val expectedGrid = createGridFromArrayStrings(listOf("...", "...", "..."))
        assertGridsEqual(expectedGrid, controller.grid, "T8.3 Two adjacent live cells die")
    }

    @Test
    fun `Grid Reset Clear - reset with empty string pattern`() {
        val controller = GolController(3, 3, initialPattern = "AAA\$AAA\$AAA", rule = standardRule)
        controller.reset("...") // Reset to all dead
        val expectedGrid = createGridFromArrayStrings(listOf("...", "...", "..."))
        assertGridsEqual(
            expectedGrid,
            controller.grid,
            "T8.4 Reset to all dead using '...' pattern",
        )
    }

    @Test
    fun `Grid Reset - reset with new pattern string`() {
        val controller = GolController(3, 3, initialPattern = "AAA", rule = standardRule)
        controller.reset(".A.\$A.A$.A.") // Reset to Tub
        val expectedTub = createGridFromArrayStrings(listOf(".A.", "A.A", ".A."))
        assertGridsEqual(expectedTub, controller.grid, "T8.4 Reset to Tub pattern string")
    }

    @Test
    fun `Grid Reset - reset with null Patterns enum (random)`() {
        // This test assumes the `reset(pattern: Patterns?)` with null generates a random grid.
        // We can't check for a specific random state, but we can check it's different.
        // And that it's still the same dimensions.
        // Note: This test relies on the stubbed Patterns enum or the actual one if available.
        // The GolController provided uses a private val DEFAULT_PATTERN of type Patterns.
        // The reset(pattern: Patterns?) is public.
        val initialPatternStr = ".A.\$A.A$.A." // Tub
        val controller =
            GolController(5, 5, initialPattern = initialPatternStr, rule = standardRule)
        val initialGridArray = controller.grid.map { it.clone() }.toTypedArray()

        // This will call the reset that uses Random.nextBoolean()
        // The `pattern: Patterns?` argument is used. If it's null, it becomes random.
        controller.reset(null as me.haroldmartin.golwallpaper.domain.Patterns?)

        assertEquals(5, controller.grid.size, "T8.4 Random reset row count check")
        assertEquals(5, controller.grid[0].size, "T8.4 Random reset col count check")
        // It's highly unlikely a 5x5 random grid will be identical to the initial Tub or all dead/all alive.
        // This is a weak test for randomness but checks the reset path.
        // A stronger test might check if *some* cells are alive and *some* are dead if grid is large enough.
        // Or simply that it's not the same as the initial specific pattern.
        var gridsAreDifferent = false
        try {
            assertGridsEqual(initialGridArray, controller.grid)
        } catch (e: AssertionError) {
            gridsAreDifferent = true // Expected for a random reset
        }
        assertTrue(
            gridsAreDifferent,
            "T8.4 Random reset should result in a different grid than initial tub",
        )
    }

    //endregion

    //region Invalid Inputs and Error Handling

    @Test
    fun `Constructor with empty pattern string`() {
        assertThrows("T9 Empty pattern string", IllegalArgumentException::class.java) {
            GolController(3, 3, initialPattern = "", rule = standardRule)
        }
    }

    @Test
    fun `Constructor with pattern string containing illegal characters`() {
        assertThrows("T9 Pattern with illegal char 'Z'", IllegalArgumentException::class.java) {
            GolController(3, 3, initialPattern = "AZA", rule = standardRule)
        }
    }
    //endregion
}
