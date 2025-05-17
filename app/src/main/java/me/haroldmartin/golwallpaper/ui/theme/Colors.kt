package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

const val RANDOM_COLOR = -2

@Suppress("MagicNumber")
sealed class Colors(val displayName: String, val value: Color) {
    object Red : Colors("Red", Color(0xFFEF5350))
    object Pink : Colors("Pink", Color(0xFFBA68C8))
    object Purple : Colors("Purple", Color(0xFF673AB7))
    object Cyan : Colors("Cyan", Color(0xFF00ACC1))
    object Green : Colors("Green", Color(0xFF4CAF50))
    object Yellow : Colors("Yellow", Color(0xFFFFEB3B))
    object Blue : Colors("Blue", Color(0xFF546E7A))
    object Black : Colors("Black", Color(0xFF212121))
    object White : Colors("White", Color(0xFFFFFFFF))

    companion object {
        val ALL: List<Colors> = listOf(Red, Pink, Purple, Cyan, Green, Yellow, Blue, Black, White)
    }
}

fun List<Colors>.chooseRandom(except: Set<Int>): Colors = this.filterNot {
    it.value.toArgb() in except
}.random()
