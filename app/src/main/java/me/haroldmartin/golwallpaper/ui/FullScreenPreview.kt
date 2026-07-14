package me.haroldmartin.golwallpaper.ui

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.haroldmartin.einkui.EinkButton
import me.haroldmartin.einkui.EinkFloatingActionButton
import me.haroldmartin.einkui.EinkTheme
import me.haroldmartin.golwallpaper.PreviewFrame
import me.haroldmartin.golwallpaper.PreviewUiState
import me.haroldmartin.golwallpaper.R
import kotlinx.coroutines.delay

internal const val PREVIEW_BUTTON_TAG = "preview_button"
internal const val PREVIEW_DIALOG_TAG = "preview_dialog"
internal const val PREVIEW_IMAGE_TAG = "preview_image"
internal const val PREVIEW_CONTROLS_TAG = "preview_controls"
internal const val PREVIEW_CONTROLS_TIMEOUT_MILLIS = 3000L

@Composable
internal fun PreviewFloatingActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    EinkFloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .testTag(PREVIEW_BUTTON_TAG),
    ) {
        Text(stringResource(R.string.preview_open))
    }
}

@Composable
@Suppress("LongParameterList")
internal fun FullScreenPreview(
    state: PreviewUiState,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onStep: () -> Unit,
    onResync: () -> Unit,
    onFramePresent: (Long) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        HideDialogSystemBars()
        FullScreenPreviewContent(
            state = state,
            onDismiss = onDismiss,
            onPlayPause = onPlayPause,
            onStep = onStep,
            onResync = onResync,
            onFramePresent = onFramePresent,
        )
    }
}

@Composable
@Suppress("LongParameterList")
internal fun FullScreenPreviewContent(
    state: PreviewUiState,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onStep: () -> Unit,
    onResync: () -> Unit,
    onFramePresent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var areControlsVisible by remember { mutableStateOf(true) }
    var controlInteraction by remember { mutableIntStateOf(0) }
    val frame = state.frame

    fun showControls() {
        areControlsVisible = true
        controlInteraction++
    }

    LaunchedEffect(areControlsVisible, controlInteraction, frame != null) {
        if (areControlsVisible && frame != null) {
            delay(PREVIEW_CONTROLS_TIMEOUT_MILLIS)
            areControlsVisible = false
        }
    }
    SideEffect {
        frame?.let { currentFrame -> onFramePresent(currentFrame.id) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(PREVIEW_DIALOG_TAG)
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showControls() })
            },
    ) {
        PreviewFrameContent(frame)
        if (areControlsVisible) {
            PreviewControls(
                state = state,
                onDismiss = onDismiss,
                onPlayPause = onPlayPause,
                onStep = onStep,
                onResync = onResync,
                onInteraction = ::showControls,
            )
        }
    }
}

@Composable
private fun BoxScope.PreviewFrameContent(frame: PreviewFrame?) {
    if (frame != null) {
        Image(
            bitmap = frame.image,
            contentDescription = stringResource(R.string.preview_fullscreen_description),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .fillMaxSize()
                .testTag(PREVIEW_IMAGE_TAG),
        )
    } else {
        Text(
            text = stringResource(R.string.preview_rendering),
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
@Suppress("LongParameterList")
private fun BoxScope.PreviewControls(
    state: PreviewUiState,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onStep: () -> Unit,
    onResync: () -> Unit,
    onInteraction: () -> Unit,
) {
    EinkButton(
        onClick = onDismiss,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(EinkTheme.spacing.medium),
    ) {
        Text(stringResource(R.string.preview_close))
    }
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(EinkTheme.spacing.medium)
            .testTag(PREVIEW_CONTROLS_TAG),
        horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.medium),
    ) {
        EinkButton(
            onClick = {
                onInteraction()
                onPlayPause()
            },
            enabled = state.frame != null,
        ) {
            Text(stringResource(if (state.isPlaying) R.string.preview_pause else R.string.preview_play))
        }
        EinkButton(
            onClick = {
                onInteraction()
                onStep()
            },
            enabled = state.frame != null && !state.isPlaying,
        ) {
            Text(stringResource(R.string.preview_step))
        }
        EinkButton(
            onClick = {
                onInteraction()
                onResync()
            },
            enabled = !state.isRendering,
        ) {
            Text(stringResource(R.string.preview_resync))
        }
    }
}

@Composable
@Suppress("AvoidVarsExceptWithDelegate")
private fun HideDialogSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        var controller: WindowInsetsControllerCompat? = null

        fun hideSystemBars(attachedView: View) {
            val window = (attachedView.parent as? DialogWindowProvider)?.window ?: return
            controller = WindowCompat.getInsetsController(window, attachedView).also { insetsController ->
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
        }

        val listener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(attachedView: View) {
                hideSystemBars(attachedView)
            }

            override fun onViewDetachedFromWindow(detachedView: View) = Unit
        }

        view.addOnAttachStateChangeListener(listener)
        if (view.isAttachedToWindow) {
            hideSystemBars(view)
        }

        onDispose {
            view.removeOnAttachStateChangeListener(listener)
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
