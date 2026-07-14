package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import me.haroldmartin.einkui.EinkColorChoice
import me.haroldmartin.einkui.EinkColorChooser
import me.haroldmartin.einkui.EinkDisclosureRow
import me.haroldmartin.einkui.EinkPaletteColor
import me.haroldmartin.einkui.EinkTheme
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR

@Composable
@Suppress("MultipleEmitters")
fun ColorPicker(label: String, selectedColor: Int, onClick: (Int) -> Unit) {
    var isPaletteVisible by remember { mutableStateOf(false) }
    val selectedChoice = selectedColor.toEinkColorChoice()

    EinkDisclosureRow(
        expanded = isPaletteVisible,
        onExpandedChange = { isPaletteVisible = it },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
            fontWeight = FontWeight.Bold,
            text = label,
        )
        if (selectedColor != RANDOM_COLOR) {
            Box(
                modifier = Modifier
                    .size(EinkTheme.spacing.medium)
                    .background(Color(selectedColor))
                    .border(EinkTheme.borders.standard, EinkTheme.colors.outline),
            )
        } else {
            Text(stringResource(R.string.random))
        }
    }
    if (isPaletteVisible) {
        Box(modifier = Modifier.padding(start = EinkTheme.spacing.medium)) {
            EinkColorChooser(
                selection = selectedChoice,
                onSelectionChange = { choice -> onClick(choice.toPersistedColor()) },
            )
        }
    }
}

private fun Int.toEinkColorChoice(): EinkColorChoice? = if (this == RANDOM_COLOR) {
    EinkColorChoice.Random
} else {
    EinkPaletteColor.entries
        .firstOrNull { paletteColor -> paletteColor.color.toArgb() == this }
        ?.let(EinkColorChoice::Palette)
}

private fun EinkColorChoice.toPersistedColor(): Int = when (this) {
    is EinkColorChoice.Palette -> value.color.toArgb()
    EinkColorChoice.Random -> RANDOM_COLOR
}
