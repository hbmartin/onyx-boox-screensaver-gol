package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.Patterns
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM
import me.haroldmartin.golwallpaper.ui.theme.SMALL

private const val SCREEN_FRACTION = 0.5f

@Suppress("MultipleEmitters")
@Composable
fun PatternPicker(onClick: (String?) -> Unit) {
    var arePatternsVisible by remember { mutableStateOf(false) }
    val patternsScrollState = rememberScrollState()

    Row(
        modifier = Modifier.clickable { arePatternsVisible = !arePatternsVisible },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Disclosure(arePatternsVisible)
        Text(
            modifier = Modifier.padding(horizontal = MEDIUM),
            fontWeight = FontWeight.Bold,
            text = stringResource(R.string.reset_pattern),
        )
    }
    if (arePatternsVisible) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SMALL),
            modifier = Modifier
                .fillMaxHeight(SCREEN_FRACTION)
                .verticalScroll(patternsScrollState)
                .padding(SMALL),
        ) {
            Button(
                onClick = { onClick(null) },
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = MEDIUM, vertical = 0.dp),
                    fontWeight = FontWeight.Normal,
                    text = stringResource(R.string.random_noise),
                )
            }
            Patterns.entries.forEach { pattern ->
                Button(
                    onClick = { onClick(pattern.value) },
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = MEDIUM, vertical = 0.dp),
                        fontWeight = FontWeight.Normal,
                        text = pattern.name,
                    )
                }
            }
        }
    }
}
