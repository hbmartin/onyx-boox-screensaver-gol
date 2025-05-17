package me.haroldmartin.golwallpaper.domain

// https://github.com/hbmartin/openrndr-game-of-life/blob/main/src/main/kotlin/GolController.kt

import kotlin.random.Random

private const val RULE_SIZE = 9
private val NEIGHBOR_RANGE = -1..1
private val DEFAULT_PATTERN = Patterns.HERRINGBONE_AGAR_P14

class GolController(
    private val rows: Int,
    private val columns: Int,
    private val birthRule: BooleanArray,
    private val surviveRule: BooleanArray,
    initialPattern: Array<BooleanArray>?,
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
        rule: String = "B3/S23",
    ) : this(
        rows = rows,
        columns = columns,
        birthRule = rule.toBirthRule(),
        surviveRule = rule.toSurviveRule(),
        initialPattern = initialPattern?.let { parsePattern(it) },
    )

    fun update(): Array<BooleanArray> {
        // n.b. if trying to avoid new Array allocation, be sure to not to update current grid for calculations
        grid =
            Array(rows) { rowIndex ->
                BooleanArray(columns) { columnIndex ->
                    val isCellAlive = grid[rowIndex][columnIndex]
                    val liveNeighbours = countLiveNeighbours(rowIndex, columnIndex)

                    if (isCellAlive) {
                        surviveRule[liveNeighbours]
                    } else {
                        birthRule[liveNeighbours]
                    }
                }
            }
        return grid
    }

    private fun countLiveNeighbours(rowIndex: Int, columnIndex: Int): Int = NEIGHBOR_RANGE
        .flatMap { y ->
            NEIGHBOR_RANGE.map { x ->
                if (x == 0 && y == 0) {
                    0
                } else {
                    val neighbourRowIndex = wrappedIndex(rowIndex + y, rows)
                    val neighbourColumnIndex = wrappedIndex(columnIndex + x, columns)

                    if (grid[neighbourRowIndex][neighbourColumnIndex]) {
                        1
                    } else {
                        0
                    }
                }
            }
        }
        .sum()

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

private fun wrappedIndex(i: Int, upperBound: Int): Int = when (i) {
    upperBound -> 0
    -1 -> upperBound - 1
    else -> i
}

// Function to convert RLE syntax to a 2D array of booleans
// eg. A.A$3.A$3.A$A2.A$.3A!
private fun parsePattern(pattern: String): Array<BooleanArray> {
    require(pattern.isNotEmpty()) { "Pattern cannot be empty" }
    val illegalChar = pattern.find { it !in "Ao.b0123456789$!\n" }
    @Suppress("NullableToStringCall")
    require(illegalChar == null) { "Illegal character in pattern: $illegalChar" }
    return pattern.split('$').mapNotNull { parseRow(it) }.toTypedArray()
}

@Suppress("AvoidMutableCollections")
private fun parseRow(pRow: String): BooleanArray? {
    if (pRow.isEmpty()) return null

    val parsed = mutableListOf<Boolean>()

    @Suppress("AvoidVarsExceptWithDelegate")
    var multiplier = 1

    @Suppress("AvoidVarsExceptWithDelegate", "BooleanPropertyNaming")
    var prevWasDigit = false
    for (char in pRow) {
        when {
            char.isDigit() -> {
                multiplier =
                    if (prevWasDigit) {
                        @Suppress("MagicNumber")
                        multiplier * 10 + char.toString().toInt()
                    } else {
                        char.toString().toInt()
                    }
                prevWasDigit = true
            }

            char == '.' || char == 'b' -> {
                repeat(multiplier) {
                    parsed.add(false)
                }
                multiplier = 1
                prevWasDigit = false
            }

            char == 'A' || char == 'o' -> {
                repeat(multiplier) {
                    parsed.add(true)
                }
                multiplier = 1
                prevWasDigit = false
            }

            else -> {
                prevWasDigit = false
            }
        }
    }
    return parsed.toBooleanArray()
}

private val Patterns.asArray: Array<BooleanArray>
    get() = parsePattern(value)
