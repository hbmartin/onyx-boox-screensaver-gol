@file:Suppress("IllegalIdentifier")

package me.haroldmartin.golwallpaper.domain

import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RleParserTest {
    private fun assertPatternEquals(
        expected: List<String>,
        actual: Array<BooleanArray>,
        message: String? = null,
    ) {
        val prefix = message?.let { "$it: " } ?: ""
        assertEquals(expected.size, actual.size, "${prefix}Row counts differ.")
        expected.forEachIndexed { r, expectedRow ->
            assertEquals(
                expectedRow.length,
                actual[r].size,
                "${prefix}Column counts differ for row $r.",
            )
            expectedRow.forEachIndexed { c, char ->
                assertEquals(char == 'A', actual[r][c], "${prefix}Cell ($r, $c) differs.")
            }
        }
    }

    @Test
    fun `parses simple pattern with A and dot`() {
        assertPatternEquals(
            expected = listOf("A.A", "..A", "AAA"),
            actual = parsePattern("A.A\$2.A\$3A!"),
        )
    }

    @Test
    fun `parses standard RLE with b and o`() {
        assertPatternEquals(
            expected = listOf(".A.", "..A", "AAA"),
            actual = parsePattern("bob\$2bo\$3o!"),
        )
    }

    @Test
    fun `parses uppercase B and O`() {
        assertPatternEquals(
            expected = listOf("..AA"),
            actual = parsePattern("2B2O!"),
        )
    }

    @Test
    fun `parses multi digit run counts`() {
        assertPatternEquals(
            expected = listOf("............A"),
            actual = parsePattern("12bo!"),
        )
    }

    @Test
    fun `ignores comment lines`() {
        assertPatternEquals(
            expected = listOf("AA", "AA"),
            actual = parsePattern("#N Block\n#C A very stable pattern\n2o\$2o!"),
            message = "Comments",
        )
    }

    @Test
    fun `ignores header line`() {
        assertPatternEquals(
            expected = listOf(".A.", "..A", "AAA"),
            actual = parsePattern("x = 3, y = 3, rule = B3/S23\nbob\$2bo\$3o!"),
            message = "Header",
        )
    }

    @Test
    fun `parses full LifeWiki style file`() {
        val rle = "#N Glider\n" +
            "#O Richard K. Guy\n" +
            "#C The smallest, most common, and first discovered spaceship.\n" +
            "x = 3, y = 3, rule = B3/S23\n" +
            "bob\$2bo\$\n" +
            "3o!"
        assertPatternEquals(
            expected = listOf(".A.", "..A", "AAA"),
            actual = parsePattern(rle),
            message = "LifeWiki file",
        )
    }

    @Test
    fun `run count on dollar produces blank rows`() {
        val parsed = parsePattern("3o3\$3o!")
        assertEquals(4, parsed.size, "3\$ should produce two blank rows between content rows")
        assertPatternEquals(listOf("AAA"), arrayOf(parsed[0]))
        assertEquals(0, parsed[1].size, "First blank row")
        assertEquals(0, parsed[2].size, "Second blank row")
        assertPatternEquals(listOf("AAA"), arrayOf(parsed[3]))
    }

    @Test
    fun `leading and trailing blank rows are trimmed`() {
        val parsed = parsePattern("2\$o2\$!")
        assertEquals(1, parsed.size, "Only the content row should remain")
        assertPatternEquals(listOf("A"), parsed)
    }

    @Test
    fun `stops parsing at terminator`() {
        assertPatternEquals(
            expected = listOf("AA"),
            actual = parsePattern("2o!3o\$5o"),
            message = "Content after ! must be ignored",
        )
    }

    @Test
    fun `handles newlines between runs`() {
        assertPatternEquals(
            expected = listOf("AAA", "AAA"),
            actual = parsePattern("3o\$\n3o!"),
            message = "Newline after row separator",
        )
    }

    @Test
    fun `pattern without terminator parses trailing row`() {
        assertPatternEquals(
            expected = listOf("A.", ".A"),
            actual = parsePattern("o.\$.o"),
        )
    }

    @Test
    fun `throws on illegal characters`() {
        assertThrows(IllegalArgumentException::class.java) {
            parsePattern("A?A")
        }
        assertThrows(IllegalArgumentException::class.java) {
            parsePattern("AZA")
        }
    }

    @Test
    fun `throws on empty pattern`() {
        assertThrows(IllegalArgumentException::class.java) {
            parsePattern("")
        }
    }

    @Test
    fun `throws on pattern with only comments`() {
        assertThrows(IllegalArgumentException::class.java) {
            parsePattern("#C nothing here")
        }
    }

    @Test
    fun `all builtin patterns parse without error`() {
        Patterns.entries.forEach { pattern ->
            val parsed = parsePattern(pattern.value)
            assertTrue(parsed.isNotEmpty(), "${pattern.name} should parse to at least one row")
            assertTrue(
                parsed.any { row -> row.any { it } },
                "${pattern.name} should contain at least one live cell",
            )
        }
    }

    @Test
    fun `blank rows are preserved when centering in controller`() {
        // "o2$o" is a live cell, a blank row, then another live cell (height 3, width 1).
        val controller = GolController(5, 5, initialPattern = "o2\$o!")
        assertEquals(true, controller[1 to 2], "Top live cell")
        assertEquals(false, controller[2 to 2], "Blank row cell")
        assertEquals(true, controller[3 to 2], "Bottom live cell")
    }
}
