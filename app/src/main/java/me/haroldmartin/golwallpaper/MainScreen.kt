package me.haroldmartin.golwallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.haroldmartin.golwallpaper.domain.Patterns
import me.haroldmartin.golwallpaper.ui.ColorPicker
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val patternsScrollState = rememberScrollState()
    var showFgColors by remember { mutableStateOf(false) }
    var showBgColors by remember { mutableStateOf(false) }
    var showPatterns by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            "NOTE! You must disable freezing in order to for the game to update:\n" +
                "Settings > Apps & Notifications > Freeze Settings > GoL Wallpaper (OFF)",
        )
        Row(
            modifier = Modifier.clickable { showFgColors = !showFgColors },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Disclosure(showFgColors)
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Bold,
                text = "Foreground Color: ",
            )
            if (uiState.fgColor != RANDOM_COLOR) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(uiState.fgColor))
                        .border(1.dp, COLOR_SCHEME.secondary),
                )
            } else {
                Text("Random")
            }
        }
        if (showFgColors) {
            ColorPicker(onClick = { color -> viewModel.setFgColor(context, color) })
        }
        Row(
            modifier = Modifier.clickable { showBgColors = !showBgColors },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Disclosure(showBgColors)
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Bold,
                text = "Background Color: ",
            )
            if (uiState.bgColor != RANDOM_COLOR) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(uiState.bgColor))
                        .border(1.dp, COLOR_SCHEME.secondary),
                )
            } else {
                Text("Random")
            }
        }
        if (showBgColors) {
            ColorPicker(onClick = { color -> viewModel.setBgColor(context, color) })
        }
        Row(
            modifier = Modifier.clickable { showPatterns = !showPatterns },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Disclosure(showPatterns)
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Bold,
                text = "Reset Starting Pattern",
            )
        }
        if (showPatterns) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .verticalScroll(patternsScrollState)
                    .padding(4.dp),
            ) {
                Button(
                    onClick = { viewModel.reset(context, null) },
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                        fontWeight = FontWeight.Normal,
                        text = "Random Noise",
                    )
                }
                Patterns.entries.forEach { pattern ->
                    Button(
                        onClick = { viewModel.reset(context, pattern.value) },
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                            fontWeight = FontWeight.Normal,
                            text = pattern.name,
                        )
                    }
                }
            }
        }
        Button(
            modifier = Modifier.border(1.dp, COLOR_SCHEME.secondary),
            onClick = { viewModel.updateGameImage(context) },
        ) {
            Text("Next Step")
        }
    }
}
