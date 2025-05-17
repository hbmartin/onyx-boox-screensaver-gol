package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.ui.theme.ColorScheme
import me.haroldmartin.golwallpaper.ui.theme.Colors

@Composable
fun ColorPicker(onClick: (Colors) -> Unit) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Colors.Companion.list.forEach { color ->
            Button(
                onClick = { onClick(color) },
                modifier = Modifier.Companion
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.value)
                    .border(1.dp, ColorScheme.secondary)
                    .padding(0.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color.value,
                ),
                content = {}
            )
        }

    }
}