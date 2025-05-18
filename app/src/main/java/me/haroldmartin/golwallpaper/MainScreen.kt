package me.haroldmartin.golwallpaper

import android.content.Context
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.haroldmartin.golwallpaper.ui.ColorPicker
import me.haroldmartin.golwallpaper.ui.PatternPicker
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME
import me.haroldmartin.golwallpaper.ui.theme.XXLARGE

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(XXLARGE),
        verticalArrangement = Arrangement.spacedBy(XXLARGE),
    ) {
        Text(stringResource(R.string.freeze_alert))
        ColorPicker(stringResource(R.string.fg_color), uiState.fgColor) { color ->
            viewModel.setFgColor(context, color)
        }
        ColorPicker(stringResource(R.string.bg_color), uiState.bgColor) { color ->
            viewModel.setBgColor(context, color)
        }
        PatternPicker { pattern ->
            viewModel.reset(context, pattern)
        }
        Button(
            modifier = Modifier.border(1.dp, COLOR_SCHEME.secondary),
            onClick = { viewModel.updateGameImage(context) },
        ) {
            Text(stringResource(R.string.next_step))
        }
        Button(
            modifier = Modifier.border(1.dp, COLOR_SCHEME.secondary),
            onClick = { openIssues(context) },
        ) {
            Text(stringResource(R.string.report_issue))
        }
    }
}

private fun openIssues(context: Context) {
    context.startActivity(
        android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            "https://github.com/hbmartin/onyx-boox-screensaver-gol/issues".toUri(),
        ),
    )
}
