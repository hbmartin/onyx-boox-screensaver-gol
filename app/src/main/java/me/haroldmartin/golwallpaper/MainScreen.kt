package me.haroldmartin.golwallpaper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.haroldmartin.golwallpaper.ui.CalendarSection
import me.haroldmartin.golwallpaper.ui.CalendarSectionCallbacks
import me.haroldmartin.golwallpaper.ui.FullScreenPreview
import me.haroldmartin.golwallpaper.ui.LayersCallbacks
import me.haroldmartin.golwallpaper.ui.LayersSection
import me.haroldmartin.golwallpaper.ui.PreviewFloatingActionButton
import me.haroldmartin.golwallpaper.ui.SettingsPanel
import me.haroldmartin.golwallpaper.ui.theme.AppButton
import me.haroldmartin.golwallpaper.ui.theme.XXLARGE
import me.haroldmartin.golwallpaper.utils.isOnyxDevice

private val PREVIEW_FAB_CLEARANCE = 80.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Suppress("LongMethod")
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewUiState by viewModel.previewUiState.collectAsStateWithLifecycle()
    val calendarUiState by viewModel.calendarUiState.collectAsStateWithLifecycle()
    val isOnyx = remember { isOnyxDevice() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.startCalendarObservation()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.pausePreview()
                viewModel.stopCalendarObservation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopCalendarObservation()
        }
    }

    LaunchedEffect(
        uiState.layers,
        uiState.bgColor,
        uiState.settings.cellSize,
        uiState.calendarOverlaySettings,
    ) {
        viewModel.resyncPreview()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            PreviewFloatingActionButton(onClick = viewModel::openPreview)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(XXLARGE)
                .padding(bottom = PREVIEW_FAB_CLEARANCE),
            verticalArrangement = Arrangement.spacedBy(XXLARGE),
        ) {
            if (isOnyx) {
                Text(stringResource(R.string.freeze_alert))
            }
            SettingsPanel(
                backgroundColor = uiState.bgColor,
                settings = uiState.settings,
                showWallpaperTarget = !isOnyx,
                onBackgroundColorChange = { color -> viewModel.setBgColor(context, color) },
                onSettingsChange = { settings -> viewModel.updateSettings(context, settings) },
            )
            CalendarSection(
                settings = uiState.calendarOverlaySettings,
                uiState = calendarUiState,
                callbacks = remember(viewModel, context) {
                    CalendarSectionCallbacks(
                        onPermissionResult = viewModel::onCalendarPermissionResult,
                        onOpenPicker = viewModel::openCalendarPicker,
                        onDisable = { viewModel.disableCalendarOverlay(context) },
                        onSettingsChange = { settings -> viewModel.updateCalendarOverlay(context, settings) },
                        onToggleDraft = viewModel::toggleDraftCalendar,
                        onConfirmPicker = { viewModel.confirmCalendarSelection(context) },
                        onDismissPicker = viewModel::dismissCalendarPicker,
                    )
                },
            )
            LayersSection(
                layers = uiState.layers,
                callbacks = LayersCallbacks(
                    onAdd = { viewModel.addLayer(context) },
                    onRemove = { index -> viewModel.removeLayer(context, index) },
                    onMoveUp = { index -> viewModel.moveLayerUp(context, index) },
                    onMoveDown = { index -> viewModel.moveLayerDown(context, index) },
                    onEnabledChange = { index, enabled ->
                        viewModel.setLayerEnabled(context, index, enabled)
                    },
                    onColorChange = { index, color ->
                        viewModel.setLayerFgColor(context, index, color)
                    },
                    onRuleChange = { index, rule ->
                        viewModel.setLayerRule(context, index, rule)
                    },
                    onResetPattern = { index, pattern, startingPattern ->
                        viewModel.resetLayer(
                            context = context,
                            index = index,
                            pattern = pattern,
                            startingPattern = startingPattern,
                        )
                    },
                ),
            )
            AppButton(
                onClick = { viewModel.openIssues(context) },
            ) {
                Text(stringResource(R.string.report_issue))
            }
        }
    }

    if (previewUiState.isOpen) {
        FullScreenPreview(
            state = previewUiState,
            onDismiss = viewModel::closePreview,
            onPlayPause = if (previewUiState.isPlaying) viewModel::pausePreview else viewModel::playPreview,
            onStep = viewModel::stepPreview,
            onResync = viewModel::resyncPreview,
            onFramePresent = viewModel::onPreviewFramePresented,
        )
    }
}
