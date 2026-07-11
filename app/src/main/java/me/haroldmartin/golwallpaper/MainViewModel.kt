package me.haroldmartin.golwallpaper

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.GolSettings
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.SaveBgColor
import me.haroldmartin.golwallpaper.domain.SaveLayers
import me.haroldmartin.golwallpaper.domain.SaveSettings
import me.haroldmartin.golwallpaper.domain.UiState
import me.haroldmartin.golwallpaper.domain.addLayer
import me.haroldmartin.golwallpaper.domain.moveDown
import me.haroldmartin.golwallpaper.domain.moveUp
import me.haroldmartin.golwallpaper.domain.parsePattern
import me.haroldmartin.golwallpaper.domain.removeLayer
import me.haroldmartin.golwallpaper.domain.resetPattern
import me.haroldmartin.golwallpaper.domain.setEnabled
import me.haroldmartin.golwallpaper.domain.setFgColor
import me.haroldmartin.golwallpaper.domain.setRule
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import me.haroldmartin.golwallpaper.utils.RenderLayer
import me.haroldmartin.golwallpaper.utils.SaveScreensaver
import me.haroldmartin.golwallpaper.utils.createCompositeBitmap
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5000L
private const val PREVIEW_DELAY_MILLIS = 500L
private const val PREVIEW_WIDTH = 360
private const val PREVIEW_HEIGHT = 480
private const val PREVIEW_ROWS = 48
private const val PREVIEW_COLUMNS = 36

@Suppress("TooManyFunctions")
class MainViewModel(
    private val saveBgColor: SaveBgColor,
    private val saveLayers: SaveLayers,
    private val saveSettings: SaveSettings,
    private val saveScreenSaver: SaveScreensaver,
) : ViewModel() {
    val uiState: StateFlow<UiState> = AppContainer.observeUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState(),
        )

    private val previewLayers = MutableStateFlow<List<PreviewLayer>>(emptyList())
    private val previewBackground = MutableStateFlow(Color.White.toArgb())
    private val _previewImage = MutableStateFlow<ImageBitmap?>(null)
    val previewImage: StateFlow<ImageBitmap?> = _previewImage.asStateFlow()
    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    init {
        viewModelScope.launch {
            _isPreviewPlaying.collectLatest { isPlaying ->
                while (isPlaying && currentCoroutineContext().isActive) {
                    advancePreview()
                    renderPreview()
                    delay(PREVIEW_DELAY_MILLIS)
                }
            }
        }
    }

    constructor() : this(
        saveBgColor = AppContainer.saveBgColor,
        saveLayers = AppContainer.saveLayers,
        saveSettings = AppContainer.saveSettings,
        saveScreenSaver = AppContainer.saveScreensaver,
    )

    fun setBgColor(context: Context, color: Int) = viewModelScope.launch {
        saveBgColor(color)
        saveScreenSaver(context, showHint = true, step = false)
    }

    fun updateSettings(context: Context, settings: GolSettings) = viewModelScope.launch {
        val current = uiState.value.settings
        val isRedrawNeeded = settings.copy(
            updateIntervalMins = current.updateIntervalMins,
            batteryThresholdPct = current.batteryThresholdPct,
        ) != current

        if (settings.cellSize != current.cellSize) {
            saveSettings.setCellSize(settings.cellSize)
        }
        if (settings.isStatsVisible != current.isStatsVisible) {
            saveSettings.setShowStats(settings.isStatsVisible)
        }
        if (settings.wallpaperTarget != current.wallpaperTarget) {
            saveSettings.setWallpaperTarget(settings.wallpaperTarget)
        }
        if (settings.batteryThresholdPct != current.batteryThresholdPct) {
            saveSettings.setBatteryThreshold(settings.batteryThresholdPct)
        }
        if (settings.updateIntervalMins != current.updateIntervalMins) {
            saveSettings.setUpdateIntervalMins(settings.updateIntervalMins)
            scheduleWallpaperUpdates(context, settings.updateIntervalMins)
        }
        if (isRedrawNeeded) {
            saveScreenSaver(context, showHint = true, step = false)
        }
    }

    fun addLayer(context: Context) = updateLayers(context) { saveLayers.addLayer() }

    fun removeLayer(context: Context, index: Int) =
        updateLayers(context) { saveLayers.removeLayer(index) }

    fun moveLayerUp(context: Context, index: Int) =
        updateLayers(context) { saveLayers.moveUp(index) }

    fun moveLayerDown(context: Context, index: Int) =
        updateLayers(context) { saveLayers.moveDown(index) }

    fun setLayerEnabled(context: Context, index: Int, isEnabled: Boolean) =
        updateLayers(context) { saveLayers.setEnabled(index, isEnabled) }

    fun setLayerFgColor(context: Context, index: Int, color: Int) =
        updateLayers(context) { saveLayers.setFgColor(index, color) }

    fun setLayerRule(context: Context, index: Int, rule: String) =
        updateLayers(context) { saveLayers.setRule(index, rule) }

    fun resetLayer(context: Context, index: Int, pattern: String?) =
        updateLayers(context) { saveLayers.resetPattern(index, pattern) }

    fun saveNextStep(context: Context) = viewModelScope.launch {
        saveScreenSaver(context, showHint = true, step = true)
    }

    fun playPreview() {
        _isPreviewPlaying.value = true
    }

    fun pausePreview() {
        _isPreviewPlaying.value = false
    }

    fun stepPreview() = viewModelScope.launch {
        advancePreview()
        renderPreview()
    }

    fun resyncPreview() = viewModelScope.launch {
        val state = uiState.value
        val background = resolvePreviewColor(
            color = state.bgColor,
            except = setOf(Color.Black.toArgb()),
        )
        previewBackground.value = background
        previewLayers.value = state.layers.map { layer -> layer.toPreviewLayer(background) }
        renderPreview()
    }

    fun openIssues(context: Context) {
        context.startActivity(
            android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                "https://github.com/hbmartin/onyx-boox-screensaver-gol/issues".toUri(),
            ),
        )
    }

    @Suppress("NoCallbacksInFunctions")
    private fun updateLayers(context: Context, update: suspend () -> Unit) =
        viewModelScope.launch {
            update()
            saveScreenSaver(context, showHint = true, step = false)
        }

    private fun advancePreview() {
        previewLayers.value = previewLayers.value.map { previewLayer ->
            if (previewLayer.isEnabled) previewLayer.controller.update()
            previewLayer
        }
    }

    private fun renderPreview() {
        val bitmap = createCompositeBitmap(
            width = PREVIEW_WIDTH,
            height = PREVIEW_HEIGHT,
            backgroundColor = previewBackground.value,
            layers = previewLayers.value.filter(PreviewLayer::isEnabled).map { previewLayer ->
                RenderLayer(
                    grid = previewLayer.controller.grid,
                    fgColor = previewLayer.fgColor,
                )
            },
        )
        _previewImage.value = bitmap.asImageBitmap()
    }

    private fun Layer.toPreviewLayer(background: Int): PreviewLayer = PreviewLayer(
        controller = previewController(state = state, rule = rule),
        fgColor = resolvePreviewColor(fgColor, except = setOf(background)),
        isEnabled = isEnabled,
    )

    @Suppress("SwallowedException")
    private fun previewController(state: String?, rule: String): GolController = try {
        if (state == null) {
            GolController(PREVIEW_ROWS, PREVIEW_COLUMNS, ".", rule).apply { reset(pattern = null) }
        } else {
            GolController(PREVIEW_ROWS, PREVIEW_COLUMNS, state.toPreviewPattern(), rule)
        }
    } catch (exception: IllegalArgumentException) {
        GolController(PREVIEW_ROWS, PREVIEW_COLUMNS, ".", rule).apply { reset(pattern = null) }
    }

    private fun resolvePreviewColor(color: Int, except: Set<Int>): Int =
        if (color == RANDOM_COLOR) Colors.ALL.chooseRandom(except).value.toArgb() else color
}

private data class PreviewLayer(
    val controller: GolController,
    val fgColor: Int,
    val isEnabled: Boolean,
)

private fun String.toPreviewPattern(): String {
    val source = parsePattern(this)
    val sourceColumns = source.maxOf { row -> row.size }
    return Array(PREVIEW_ROWS) { row ->
        BooleanArray(PREVIEW_COLUMNS) { column ->
            val sourceRow = source[(row * source.size) / PREVIEW_ROWS]
            sourceRow.getOrElse((column * sourceColumns) / PREVIEW_COLUMNS) { false }
        }
    }.joinToString("$") { row ->
        row.joinToString("") { isAlive -> if (isAlive) "A" else "." }
    }
}
