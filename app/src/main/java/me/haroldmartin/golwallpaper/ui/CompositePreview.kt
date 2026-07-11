package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.ui.theme.AppButton
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM

private const val PREVIEW_ASPECT_RATIO = 0.75f

@Composable
@Suppress("LongParameterList")
fun CompositePreview(
    image: ImageBitmap?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStep: () -> Unit,
    onResync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MEDIUM),
    ) {
        Text(stringResource(R.string.preview_title))
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = stringResource(R.string.preview_title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(PREVIEW_ASPECT_RATIO)
                    .border(1.dp, COLOR_SCHEME.secondary),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MEDIUM)) {
            AppButton(onClick = onPlayPause) {
                Text(stringResource(if (isPlaying) R.string.preview_pause else R.string.preview_play))
            }
            AppButton(onClick = onStep, enabled = !isPlaying) {
                Text(stringResource(R.string.preview_step))
            }
            AppButton(onClick = onResync) {
                Text(stringResource(R.string.preview_resync))
            }
        }
    }
}
