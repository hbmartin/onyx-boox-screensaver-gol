package me.haroldmartin.golwallpaper.domain

// https://github.com/hbmartin/openrndr-game-of-life/blob/main/src/main/kotlin/GolController.kt

import kotlin.random.Random

private const val RULE_SIZE = 9
private val DEFAULT_PATTERN = Patterns.HERRINGBONE_AGAR_P14
private val HEADER_REGEX = Regex("""^\s*x\s*=.*""", RegexOption.IGNORE_CASE)
private const val DECIMAL_BASE = 10
private const val MAX_PATTERN_LENGTH = 100_000

class GolController(
    private val rows: Int,
    private val columns: Int,
    private val birthRule: BooleanArray,
    private val surviveRule: BooleanArray,
    initialPattern: Array<BooleanArray>?,
    private val areEdgesWrapped: Boolean = DEFAULT_WRAP_EDGES,
) {
    @Suppress("AvoidVarsExceptWithDelegate")
    var grid: Array<BooleanArray> = initialPattern?.let {
        centerPattern(it, rows, columns)
    } ?: centerPattern(
        initialPattern = parsePattern(DEFAULT_PATTERN.value),
        rows = rows,
        columns = columns,
    )
        private set

    @Suppress("AvoidVarsExceptWithDelegate")
    private var buffer: Array<BooleanArray> = Array(rows) { BooleanArray(columns) }

    val population: Int
        get() {
            @Suppress("AvoidVarsExceptWithDelegate")
            var count = 0
            for (row in grid) {
                for (cell in row) {
                    if (cell) count++
                }
            }
            return count
        }

    init {
        require(columns > 0) { "Columns must be greater than 0" }
        require(rows > 0) { "Rows must be greater than 0" }
        require(birthRule.size == RULE_SIZE) { "Birth rule must be of size $RULE_SIZE" }
        require(surviveRule.size == RULE_SIZE) { "Survive rule must be of size $RULE_SIZE" }
    }

    constructor(
        rows: Int,
        columns: Int,
        initialPattern: String?,
        rule: String = DEFAULT_RULE,
        areEdgesWrapped: Boolean = DEFAULT_WRAP_EDGES,
    ) : this(
        rows = rows,
        columns = columns,
        birthRule = rule.toBirthRule(),
        surviveRule = rule.toSurviveRule(),
        initialPattern = initialPattern?.let { parsePattern(it) },
        areEdgesWrapped = areEdgesWrapped,
    )

    // Neighbour counting is deliberately allocation-free: this runs over every cell of a
    // screen-sized grid.
    @Suppress(
        "AvoidVarsExceptWithDelegate",
        "NestedBlockDepth",
        "CyclomaticComplexMethod",
        "CognitiveComplexMethod",
    )
    fun update(): Array<BooleanArray> {
        // Double buffered: compute the next generation into `buffer`, then swap it with `grid`
        // so each update allocates nothing.
        val next = buffer
        if (areEdgesWrapped) {
            updateWrapped(next)
        } else {
            updateDeadEdges(next)
        }
        buffer = grid
        grid = next
        return grid
    }

    @Suppress("AvoidVarsExceptWithDelegate")
    private fun updateWrapped(next: Array<BooleanArray>) {
        for (rowIndex in 0 until rows) {
            val above = grid[if (rowIndex == 0) rows - 1 else rowIndex - 1]
            val current = grid[rowIndex]
            val below = grid[if (rowIndex == rows - 1) 0 else rowIndex + 1]
            val nextRow = next[rowIndex]
            for (columnIndex in 0 until columns) {
                val liveNeighbours = countWrappedNeighbours(
                    above = above,
                    current = current,
                    below = below,
                    columnIndex = columnIndex,
                    columns = columns,
                )
                nextRow[columnIndex] = nextCellState(current[columnIndex], liveNeighbours)
            }
        }
    }

    private fun updateDeadEdges(next: Array<BooleanArray>) {
        for (rowIndex in 0 until rows) {
            val nextRow = next[rowIndex]
            for (columnIndex in 0 until columns) {
                val liveNeighbours = grid.countDeadEdgeNeighbours(
                    rowIndex = rowIndex,
                    columnIndex = columnIndex,
                    rows = rows,
                    columns = columns,
                )
                nextRow[columnIndex] = nextCellState(grid[rowIndex][columnIndex], liveNeighbours)
            }
        }
    }

    private fun nextCellState(isAlive: Boolean, liveNeighbours: Int): Boolean =
        if (isAlive) surviveRule[liveNeighbours] else birthRule[liveNeighbours]

    fun turnOnCell(rowIndex: Int, colIndex: Int) {
        grid[rowIndex][colIndex] = true
    }

    operator fun get(coords: Pair<Int, Int>): Boolean = grid[coords.first][coords.second]

    override fun toString(): String = grid.joinToString("$\n") {
        it.compress()
    }

    fun reset(pattern: Patterns?) {
        grid =
            if (pattern != null) {
                centerPattern(pattern.asArray, rows, columns)
            } else {
                val random = Array(rows) {
                    BooleanArray(columns) {
                        Random.nextBoolean()
                    }
                }

                centerPattern(random, rows, columns)
            }
    }

    fun reset(pattern: String) {
        grid = centerPattern(parsePattern(pattern), rows, columns)
    }
}

@Suppress("AvoidVarsExceptWithDelegate")
private fun countWrappedNeighbours(
    above: BooleanArray,
    current: BooleanArray,
    below: BooleanArray,
    columnIndex: Int,
    columns: Int,
): Int {
    val left = if (columnIndex == 0) columns - 1 else columnIndex - 1
    val right = if (columnIndex == columns - 1) 0 else columnIndex + 1
    var liveNeighbours = 0
    if (above[left]) liveNeighbours++
    if (above[columnIndex]) liveNeighbours++
    if (above[right]) liveNeighbours++
    if (current[left]) liveNeighbours++
    if (current[right]) liveNeighbours++
    if (below[left]) liveNeighbours++
    if (below[columnIndex]) liveNeighbours++
    if (below[right]) liveNeighbours++
    return liveNeighbours
}

@Suppress("AvoidVarsExceptWithDelegate", "NestedBlockDepth")
private fun Array<BooleanArray>.countDeadEdgeNeighbours(
    rowIndex: Int,
    columnIndex: Int,
    rows: Int,
    columns: Int,
): Int {
    var liveNeighbours = 0
    val firstRow = maxOf(0, rowIndex - 1)
    val lastRow = minOf(rows - 1, rowIndex + 1)
    val firstColumn = maxOf(0, columnIndex - 1)
    val lastColumn = minOf(columns - 1, columnIndex + 1)
    for (neighbourRow in firstRow..lastRow) {
        for (neighbourColumn in firstColumn..lastColumn) {
            if ((neighbourRow != rowIndex || neighbourColumn != columnIndex) &&
                this[neighbourRow][neighbourColumn]
            ) {
                liveNeighbours++
            }
        }
    }
    return liveNeighbours
}

@Suppress("AvoidVarsExceptWithDelegate")
private fun BooleanArray.compress(): String {
    if (this.isEmpty()) return ""

    val result = StringBuilder()
    var count = 1
    var currentChar = this[0].asChar()

    for (i in 1 until this.size) {
        if (this[i].asChar() == currentChar) {
            count++
        } else {
            if (count > 1) result.append(count)
            result.append(currentChar)
            currentChar = this[i].asChar()
            count = 1
        }
    }

    if (count > 1) result.append(count)
    result.append(currentChar)

    return result.toString()
}

private fun Boolean.asChar(): Char = when (this) {
    true -> 'A'
    false -> '.'
}

private fun String.toBirthRule(): BooleanArray {
    val rule = this.uppercase().substringAfter("B").substringBefore("/")
    return BooleanArray(RULE_SIZE) { it.toString() in rule }
}

private fun String.toSurviveRule(): BooleanArray {
    val rule = this.uppercase().substringAfter("S")
    return BooleanArray(RULE_SIZE) { it.toString() in rule }
}

private fun centerPattern(
    initialPattern: Array<BooleanArray>,
    rows: Int,
    columns: Int,
): Array<BooleanArray> {
    require(initialPattern.isNotEmpty()) { "Initial shape cannot be empty" }
    require(initialPattern.size <= rows) {
        "Initial shape is too tall (${initialPattern.size} > $rows)"
    }
    require(initialPattern[0].isNotEmpty()) { "Initial shape has 0 width columns" }

    val height = initialPattern.size
    val width = requireNotNull(initialPattern.maxOfOrNull { it.size })
    require(width <= columns) { "Initial shape is too wide ($width > $columns)" }
    val startRow = (rows - height) / 2
    val startColumn = (columns - width) / 2

    return Array(rows) { rowIndex ->
        BooleanArray(columns) { columnIndex ->
            if (rowIndex in startRow until startRow + height &&
                columnIndex in startColumn until startColumn + width
            ) {
                initialPattern[rowIndex - startRow]
                    .getOrElse(columnIndex - startColumn) { false }
            } else {
                false
            }
        }
    }
}

// Converts run length encoded (RLE) text to a 2D array of booleans, eg. A.A$3.A$3.A$A2.A$.3A!
// Handles the full Golly/LifeWiki RLE format: `#` comment lines, an optional `x = m, y = n,
// rule = ...` header line, run counts on `$` (multiple blank rows), the `!` terminator, and
// line wrapping anywhere. https://golly.sourceforge.io/Help/formats.html
@Suppress("AvoidMutableCollections", "AvoidVarsExceptWithDelegate", "CyclomaticComplexMethod")
internal fun parsePattern(pattern: String): Array<BooleanArray> {
    require(pattern.isNotEmpty()) { "Pattern cannot be empty" }
    val body = pattern.lineSequence()
        .filterNot { line ->
            line.trimStart().startsWith("#") || HEADER_REGEX.matches(line)
        }
        .joinToString("\n")

    val parsedRows = mutableListOf<BooleanArray>()
    val currentRow = mutableListOf<Boolean>()
    var runCount = 0

    loop@ for (char in body) {
        when {
            char.isDigit() -> {
                runCount = runCount * DECIMAL_BASE + (char - '0')
            }

            char == '.' || char == 'b' || char == 'B' -> {
                repeat(runCount.coerceAtLeast(1)) { currentRow.add(false) }
                runCount = 0
            }

            char == 'A' || char == 'o' || char == 'O' -> {
                repeat(runCount.coerceAtLeast(1)) { currentRow.add(true) }
                runCount = 0
            }

            char == '$' -> {
                parsedRows.add(currentRow.toBooleanArray())
                currentRow.clear()
                // A run count on `$` means (count - 1) additional blank rows.
                repeat(runCount.coerceAtLeast(1) - 1) { parsedRows.add(BooleanArray(0)) }
                runCount = 0
            }

            char == '!' -> {
                break@loop
            }

            char.isWhitespace() -> {
                // Line wrapping is allowed anywhere, including within a run count.
            }

            else -> {
                throw IllegalArgumentException("Illegal character in pattern: $char")
            }
        }
    }
    if (currentRow.isNotEmpty()) {
        parsedRows.add(currentRow.toBooleanArray())
    }
    // Leading/trailing blank rows carry no information and would break centering.
    val trimmedRows = parsedRows.dropWhile { it.isEmpty() }.dropLastWhile { it.isEmpty() }
    require(trimmedRows.isNotEmpty()) { "Pattern contains no rows" }
    return trimmedRows.toTypedArray()
}

private val Patterns.asArray: Array<BooleanArray>
    get() = parsePattern(value)

// Validates user-pasted RLE before it is applied, so an unparseable paste can be rejected
// in the UI instead of silently falling back to a random grid at render time.
@Suppress("SwallowedException")
internal fun isParseablePattern(pattern: String): Boolean =
    if (pattern.length > MAX_PATTERN_LENGTH) {
        false
    } else {
        try {
            parsePattern(pattern)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
