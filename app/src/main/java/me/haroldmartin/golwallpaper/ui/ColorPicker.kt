package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.LARGE
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR

@Composable
fun ColorPicker(label: String, selectedColor: Int, onClick: (Int) -> Unit) {
    var isPaletteVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.clickable { isPaletteVisible = !isPaletteVisible },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Disclosure(isPaletteVisible)
        Text(
            modifier = Modifier.padding(horizontal = MEDIUM),
            fontWeight = FontWeight.Bold,
            text = label,
        )
        if (selectedColor != RANDOM_COLOR) {
            Box(
                modifier = Modifier
                    .size(LARGE)
                    .background(Color(selectedColor))
                    .border(1.dp, COLOR_SCHEME.secondary),
            )
        } else {
            Text(stringResource(R.string.random))
        }
    }
    if (isPaletteVisible) {
        ColorPicker(onClick = { color -> onClick(color) })
    }
}
