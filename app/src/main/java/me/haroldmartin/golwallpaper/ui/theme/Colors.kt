package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.ui.graphics.toArgb
import me.haroldmartin.einkui.EinkPaletteColor

const val RANDOM_COLOR = -2

val ALL_COLORS: List<EinkPaletteColor> = EinkPaletteColor.entries

fun List<EinkPaletteColor>.chooseRandom(except: Set<Int>): EinkPaletteColor = this.filterNot {
    it.color.toArgb() in except
}.random()
