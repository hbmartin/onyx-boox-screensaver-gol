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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR

@Composable
fun ColorPicker(onClick: (Int) -> Unit) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Colors.Companion.ALL.forEach { color ->
            Button(
                onClick = { onClick(color.value.toArgb()) },
                modifier = Modifier.Companion
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.value)
                    .border(1.dp, COLOR_SCHEME.secondary)
                    .padding(0.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color.value,
                ),
                content = {},
            )
        }
        Button(
            onClick = { onClick(RANDOM_COLOR) },
            modifier = Modifier.Companion
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(1.dp, COLOR_SCHEME.secondary)
                .padding(0.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
            ),
        ) {
            Text(
                "RAN\nDOM",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
        }
    }
}
