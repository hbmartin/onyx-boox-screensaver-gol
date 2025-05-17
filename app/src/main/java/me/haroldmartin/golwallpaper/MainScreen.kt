package me.haroldmartin.golwallpaper

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.haroldmartin.golwallpaper.ui.ColorPicker
import me.haroldmartin.golwallpaper.ui.PatternPicker
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        ColorPicker("Foreground Color", uiState.fgColor) { color ->
            viewModel.setFgColor(context, color)
        }
        ColorPicker("Background Color", uiState.bgColor) { color ->
            viewModel.setBgColor(context, color)
        }
        PatternPicker { pattern ->
            viewModel.reset(context, pattern)
        }
        Button(
            modifier = Modifier.border(1.dp, COLOR_SCHEME.secondary),
            onClick = { viewModel.updateGameImage(context) },
        ) {
            Text("Generate Next Step")
        }
    }
}
