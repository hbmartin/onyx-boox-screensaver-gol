package me.haroldmartin.golwallpaper

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.haroldmartin.golwallpaper.ui.ColorPicker
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            ColorPicker(onClick = { color -> viewModel.setFgColor(context, color) })
            Button(
                modifier = Modifier.border(1.dp, COLOR_SCHEME.secondary),
                onClick = { viewModel.updateGameImage(context) },
            ) {
                Text("Save Image")
            }
        }
    }
}
