package me.haroldmartin.golwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.haroldmartin.golwallpaper.domain.CalendarAgenda
import me.haroldmartin.golwallpaper.domain.CalendarAgendaResult
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.CalendarRepository
import me.haroldmartin.golwallpaper.domain.CalendarSettingsStore
import me.haroldmartin.golwallpaper.domain.CalendarSource
import me.haroldmartin.golwallpaper.domain.DEFAULT_CELL_SIZE
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.GolSettings
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.LoadCalendarAgenda
import me.haroldmartin.golwallpaper.domain.SaveBgColor
import me.haroldmartin.golwallpaper.domain.SaveLayers
import me.haroldmartin.golwallpaper.domain.SaveSettings
import me.haroldmartin.golwallpaper.domain.UiState
import me.haroldmartin.golwallpaper.domain.addLayer
import me.haroldmartin.golwallpaper.domain.initialCalendarSelection
import me.haroldmartin.golwallpaper.domain.moveDown
import me.haroldmartin.golwallpaper.domain.moveUp
import me.haroldmartin.golwallpaper.domain.removeLayer
import me.haroldmartin.golwallpaper.domain.resetPattern
import me.haroldmartin.golwallpaper.domain.setEnabled
import me.haroldmartin.golwallpaper.domain.setFgColor
import me.haroldmartin.golwallpaper.domain.setRule
import me.haroldmartin.golwallpaper.ui.theme.Colors
import me.haroldmartin.golwallpaper.ui.theme.RANDOM_COLOR
import me.haroldmartin.golwallpaper.ui.theme.chooseRandom
import me.haroldmartin.golwallpaper.utils.DeviceOverlays
import me.haroldmartin.golwallpaper.utils.RenderLayer
import me.haroldmartin.golwallpaper.utils.RenderStats
import me.haroldmartin.golwallpaper.utils.Resolution
import me.haroldmartin.golwallpaper.utils.SaveScreensaver
import me.haroldmartin.golwallpaper.utils.getScreenResolution
import me.haroldmartin.golwallpaper.utils.height
import me.haroldmartin.golwallpaper.utils.renderDeviceBitmap
import me.haroldmartin.golwallpaper.utils.toRowsCols
import me.haroldmartin.golwallpaper.utils.width
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainViewModel"
private const val STOP_TIMEOUT_MILLIS = 5000L
private const val PREVIEW_DELAY_MILLIS = 500L
private const val PREVIEW_FALLBACK_WIDTH = 360
private const val PREVIEW_FALLBACK_HEIGHT = 480
private const val PREVIEW_BUFFER_COUNT = 2
private const val CALENDAR_CHANGE_DEBOUNCE_MILLIS = 500L

enum class CalendarUiIssue {
    PERMISSION_REQUIRED,
    NO_CALENDARS,
    SOURCES_UNAVAILABLE,
}

data class CalendarUiState(
    val sources: List<CalendarSource> = emptyList(),
    val draftSelectedIds: Set<Long> = emptySet(),
    val isPickerVisible: Boolean = false,
    val issue: CalendarUiIssue? = null,
)

data class PreviewFrame(
    val id: Long,
    val image: ImageBitmap,
    internal val bufferIndex: Int,
)

data class PreviewUiState(
    val isOpen: Boolean = false,
    val isRendering: Boolean = false,
    val isPlaying: Boolean = false,
    val frame: PreviewFrame? = null,
)

@Suppress("TooManyFunctions", "LongParameterList")
class MainViewModel(
    private val saveBgColor: SaveBgColor,
    private val saveLayers: SaveLayers,
    private val saveSettings: SaveSettings,
    private val saveScreenSaver: SaveScreensaver,
    private val calendarSettingsStore: CalendarSettingsStore,
    private val calendarRepository: CalendarRepository,
    private val loadCalendarAgenda: LoadCalendarAgenda,
    private val appContext: Context,
    private val renderDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val uiState: StateFlow<UiState> = AppContainer.observeUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState(),
        )

    private val previewLayers = MutableStateFlow<List<PreviewLayer>>(emptyList())
    private val previewBackground = MutableStateFlow(Color.White.toArgb())
    private val _previewUiState = MutableStateFlow(PreviewUiState())
    val previewUiState: StateFlow<PreviewUiState> = _previewUiState.asStateFlow()
    private val previewAgenda = MutableStateFlow<CalendarAgenda?>(null)
    private val _calendarUiState = MutableStateFlow(CalendarUiState())
    val calendarUiState: StateFlow<CalendarUiState> = _calendarUiState.asStateFlow()

    @Suppress("AvoidVarsExceptWithDelegate")
    private var previewGeometry = PreviewGeometry()

    @Suppress("AvoidVarsExceptWithDelegate")
    private var previewRenderJob: Job? = null

    @Suppress("AvoidVarsExceptWithDelegate")
    private var previewBufferPool: PreviewBufferPool? = null

    @Suppress("AvoidVarsExceptWithDelegate")
    private var previewRenderRequested = false

    @Suppress("AvoidVarsExceptWithDelegate")
    private var nextPreviewFrameId = 0L

    @Suppress("AvoidVarsExceptWithDelegate")
    private var previewSessionId = 0L

    @Suppress("AvoidVarsExceptWithDelegate")
    private var calendarObservationJob: Job? = null

    init {
        viewModelScope.launch {
            previewUiState.map { state -> state.isPlaying }
                .distinctUntilChanged()
                .collectLatest { isPlaying ->
                    while (isPlaying && previewUiState.value.isOpen && currentCoroutineContext().isActive) {
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
        calendarSettingsStore = AppContainer.calendarPreferences,
        calendarRepository = AppContainer.calendarRepository,
        loadCalendarAgenda = AppContainer.loadCalendarAgenda,
        appContext = AppContainer.applicationContext,
        renderDispatcher = AppContainer.defaultDispatcher(),
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

    fun openPreview() = viewModelScope.launch {
        if (previewUiState.value.isOpen) return@launch
        val sessionId = ++previewSessionId
        _previewUiState.value = PreviewUiState(isOpen = true, isRendering = true)
        syncPreviewState()
        preparePreviewBufferPool(sessionId)
        if (previewUiState.value.isOpen && previewSessionId == sessionId) {
            renderPreview()
        }
    }

    fun closePreview() {
        previewSessionId++
        previewRenderJob?.cancel()
        previewRenderJob = null
        previewRenderRequested = false
        previewBufferPool = null
        _previewUiState.value = PreviewUiState()
    }

    fun playPreview() {
        _previewUiState.update { state ->
            if (state.isOpen && state.frame != null) state.copy(isPlaying = true) else state
        }
    }

    fun pausePreview() {
        _previewUiState.update { state -> state.copy(isPlaying = false) }
    }

    fun stepPreview() = viewModelScope.launch {
        if (!previewUiState.value.isOpen || previewUiState.value.isPlaying) return@launch
        advancePreview()
        renderPreview()
    }

    fun resyncPreview() = viewModelScope.launch {
        syncPreviewState()
        if (previewUiState.value.isOpen) {
            preparePreviewBufferPool(previewSessionId)
            renderPreview()
        }
    }

    fun onPreviewFramePresented(frameId: Long) {
        val pool = previewBufferPool ?: return
        if (pool.markPresented(frameId) && previewRenderRequested) {
            previewRenderRequested = false
            renderPreview()
        }
    }

    private suspend fun syncPreviewState() {
        val state = uiState.value
        previewGeometry = createPreviewGeometry(
            resolution = getScreenResolution(appContext),
            cellSize = state.settings.cellSize,
        )
        val background = resolvePreviewColor(
            color = state.bgColor,
            except = setOf(Color.Black.toArgb()),
        )
        previewBackground.value = background
        previewLayers.value = state.layers.map { layer ->
            layer.toPreviewLayer(background = background, geometry = previewGeometry)
        }
        refreshCalendarAgenda()
    }

    private suspend fun preparePreviewBufferPool(sessionId: Long) {
        val resolution = previewGeometry.bitmapResolution
        val currentPool = previewBufferPool
        if (currentPool?.resolution == resolution) return
        val newPool = withContext(renderDispatcher) { PreviewBufferPool(resolution) }
        if (previewUiState.value.isOpen && previewSessionId == sessionId) {
            previewBufferPool = newPool
            _previewUiState.update { state -> state.copy(frame = null, isRendering = true) }
        }
    }

    fun onCalendarPermissionResult(granted: Boolean) {
        if (granted) {
            openCalendarPicker()
        } else {
            _calendarUiState.value = _calendarUiState.value.copy(
                isPickerVisible = false,
                issue = CalendarUiIssue.PERMISSION_REQUIRED,
            )
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun openCalendarPicker() = viewModelScope.launch {
        if (!calendarRepository.hasPermission()) {
            _calendarUiState.value = _calendarUiState.value.copy(
                issue = CalendarUiIssue.PERMISSION_REQUIRED,
            )
            return@launch
        }
        val sources = try {
            calendarRepository.getCalendars()
        } catch (exception: Exception) {
            currentCoroutineContext().ensureActive()
            _calendarUiState.value = _calendarUiState.value.copy(
                issue = CalendarUiIssue.SOURCES_UNAVAILABLE,
            )
            return@launch
        }
        if (sources.isEmpty()) {
            _calendarUiState.value = _calendarUiState.value.copy(
                sources = emptyList(),
                isPickerVisible = false,
                issue = CalendarUiIssue.NO_CALENDARS,
            )
            return@launch
        }
        val initialSelection = initialCalendarSelection(
            sources = sources,
            storedIds = uiState.value.calendarOverlaySettings.selectedCalendarIds,
        )
        _calendarUiState.value = CalendarUiState(
            sources = sources,
            draftSelectedIds = initialSelection,
            isPickerVisible = true,
        )
    }

    fun toggleDraftCalendar(calendarId: Long) {
        val current = _calendarUiState.value
        val selected = if (calendarId in current.draftSelectedIds) {
            current.draftSelectedIds - calendarId
        } else {
            current.draftSelectedIds + calendarId
        }
        _calendarUiState.value = current.copy(draftSelectedIds = selected)
    }

    fun dismissCalendarPicker() {
        _calendarUiState.value = _calendarUiState.value.copy(isPickerVisible = false)
    }

    fun confirmCalendarSelection(context: Context) = viewModelScope.launch {
        val selectedIds = _calendarUiState.value.draftSelectedIds
        val updated = uiState.value.calendarOverlaySettings.copy(
            isEnabled = selectedIds.isNotEmpty(),
            selectedCalendarIds = selectedIds,
        )
        calendarSettingsStore.save(updated)
        _calendarUiState.value = _calendarUiState.value.copy(
            isPickerVisible = false,
            issue = null,
        )
        refreshCalendarAgenda(updated)
        renderPreview()
        saveScreenSaver(context, showHint = true, step = false)
    }

    fun disableCalendarOverlay(context: Context) = viewModelScope.launch {
        val updated = uiState.value.calendarOverlaySettings.copy(isEnabled = false)
        calendarSettingsStore.save(updated)
        refreshCalendarAgenda(updated)
        renderPreview()
        saveScreenSaver(context, showHint = true, step = false)
    }

    fun updateCalendarOverlay(
        context: Context,
        settings: CalendarOverlaySettings,
    ) = viewModelScope.launch {
        calendarSettingsStore.save(settings)
        refreshCalendarAgenda(settings)
        renderPreview()
        saveScreenSaver(context, showHint = true, step = false)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun startCalendarObservation() {
        if (calendarObservationJob != null) return
        calendarObservationJob = viewModelScope.launch {
            val initialAgenda = previewAgenda.value
            refreshCalendarAgenda()
            if (previewAgenda.value != initialAgenda) {
                renderPreview()
                if (uiState.value.calendarOverlaySettings.isEnabled) {
                    saveScreenSaver(appContext, showHint = false, step = false)
                }
            }
            uiState.map { state -> state.calendarOverlaySettings.isEnabled }
                .distinctUntilChanged()
                .flatMapLatest { isEnabled ->
                    if (isEnabled && calendarRepository.hasPermission()) {
                        calendarRepository.observeChanges()
                    } else {
                        emptyFlow()
                    }
                }
                .debounce(CALENDAR_CHANGE_DEBOUNCE_MILLIS)
                .collect {
                    val oldAgenda = previewAgenda.value
                    refreshCalendarAgenda()
                    if (previewAgenda.value != oldAgenda) {
                        renderPreview()
                        saveScreenSaver(appContext, showHint = false, step = false)
                    }
                }
        }
    }

    fun stopCalendarObservation() {
        calendarObservationJob?.cancel()
        calendarObservationJob = null
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
            if (previewLayer.isEnabled) {
                previewLayer.controller.update()
                previewLayer.copy(generation = previewLayer.generation + 1)
            } else {
                previewLayer
            }
        }
    }

    private fun renderPreview() {
        if (!previewUiState.value.isOpen) return
        val pool = previewBufferPool
        val buffer = pool?.acquire()
        if (pool == null || buffer == null) {
            previewRenderRequested = true
            return
        }
        previewRenderRequested = false
        val request = createPreviewRenderRequest()
        val sessionId = previewSessionId
        _previewUiState.update { state -> state.copy(isRendering = state.frame == null) }
        previewRenderJob = viewModelScope.launch(renderDispatcher) {
            renderPreviewFrame(
                pool = pool,
                buffer = buffer,
                request = request,
                sessionId = sessionId,
            )
        }
    }

    private fun createPreviewRenderRequest(): PreviewRenderRequest {
        val background = previewBackground.value
        val currentUiState = uiState.value
        val currentPreviewLayers = previewLayers.value
        val layers = currentPreviewLayers.filter(PreviewLayer::isEnabled).map { previewLayer ->
            RenderLayer(
                grid = Array(previewLayer.controller.grid.size) { row ->
                    previewLayer.controller.grid[row].clone()
                },
                fgColor = previewLayer.fgColor,
            )
        }
        val stats = if (currentUiState.settings.isStatsVisible) {
            RenderStats(
                generation = currentPreviewLayers.filter(PreviewLayer::isEnabled)
                    .maxOfOrNull(PreviewLayer::generation)
                    ?: 0,
                population = currentPreviewLayers.filter(PreviewLayer::isEnabled)
                    .sumOf { previewLayer -> previewLayer.controller.population },
            )
        } else {
            null
        }
        return PreviewRenderRequest(
            background = background,
            layers = layers,
            overlays = DeviceOverlays(
                calendarAgenda = previewAgenda.value,
                calendarSettings = currentUiState.calendarOverlaySettings,
                stats = stats,
            ),
        )
    }

    @Suppress("AvoidVarsExceptWithDelegate", "TooGenericExceptionCaught")
    private suspend fun renderPreviewFrame(
        pool: PreviewBufferPool,
        buffer: PreviewBuffer,
        request: PreviewRenderRequest,
        sessionId: Long,
    ) {
        var isPublished = false
        try {
            renderDeviceBitmap(
                context = appContext,
                bitmap = buffer.bitmap,
                backgroundColor = request.background,
                layers = request.layers,
                overlays = request.overlays,
            )
            currentCoroutineContext().ensureActive()
            if (!previewUiState.value.isOpen || previewSessionId != sessionId) return
            val frame = pool.publish(buffer, ++nextPreviewFrameId)
            isPublished = true
            _previewUiState.update { state ->
                if (state.isOpen && previewSessionId == sessionId) {
                    state.copy(isRendering = false, frame = frame)
                } else {
                    state
                }
            }
        } catch (exception: Exception) {
            currentCoroutineContext().ensureActive()
            Log.e(TAG, "Failed to render preview", exception)
            _previewUiState.update { state -> state.copy(isRendering = false) }
        } finally {
            if (!isPublished) pool.release(buffer)
        }
    }

    private suspend fun refreshCalendarAgenda(
        settings: CalendarOverlaySettings = uiState.value.calendarOverlaySettings,
    ) {
        when (val result = loadCalendarAgenda(settings)) {
            is CalendarAgendaResult.Available -> {
                previewAgenda.value = result.agenda
                _calendarUiState.value = _calendarUiState.value.copy(issue = null)
            }
            CalendarAgendaResult.PermissionMissing -> {
                previewAgenda.value = null
                _calendarUiState.value = _calendarUiState.value.copy(
                    issue = if (settings.isEnabled) CalendarUiIssue.PERMISSION_REQUIRED else null,
                )
            }
            CalendarAgendaResult.SourcesUnavailable -> {
                previewAgenda.value = null
                _calendarUiState.value = _calendarUiState.value.copy(
                    issue = if (settings.isEnabled) CalendarUiIssue.SOURCES_UNAVAILABLE else null,
                )
            }
        }
    }

    private fun Layer.toPreviewLayer(background: Int, geometry: PreviewGeometry): PreviewLayer =
        PreviewLayer(
            controller = previewController(
                state = state,
                rule = rule,
                rows = geometry.rows,
                columns = geometry.columns,
            ),
            fgColor = resolvePreviewColor(fgColor, except = setOf(background)),
            isEnabled = isEnabled,
            generation = generation,
        )

    @Suppress("SwallowedException")
    private fun previewController(
        state: String?,
        rule: String,
        rows: Int,
        columns: Int,
    ): GolController = try {
        if (state == null) {
            GolController(
                rows = rows,
                columns = columns,
                initialPattern = ".",
                rule = rule,
            ).apply { reset(pattern = null) }
        } else {
            GolController(
                rows = rows,
                columns = columns,
                initialPattern = state,
                rule = rule,
            )
        }
    } catch (exception: IllegalArgumentException) {
        GolController(
            rows = rows,
            columns = columns,
            initialPattern = ".",
            rule = rule,
        ).apply { reset(pattern = null) }
    }

    private fun resolvePreviewColor(color: Int, except: Set<Int>): Int =
        if (color == RANDOM_COLOR) Colors.ALL.chooseRandom(except).value.toArgb() else color
}

private data class PreviewLayer(
    val controller: GolController,
    val fgColor: Int,
    val isEnabled: Boolean,
    val generation: Int,
)

internal data class PreviewGeometry(
    val bitmapResolution: Resolution = Resolution(PREVIEW_FALLBACK_WIDTH, PREVIEW_FALLBACK_HEIGHT),
    val rows: Int = PREVIEW_FALLBACK_HEIGHT / DEFAULT_CELL_SIZE,
    val columns: Int = PREVIEW_FALLBACK_WIDTH / DEFAULT_CELL_SIZE,
)

internal fun createPreviewGeometry(resolution: Resolution, cellSize: Int): PreviewGeometry {
    val wallpaperResolution = resolution.takeIf { it.width > 0 && it.height > 0 }
        ?: Resolution(PREVIEW_FALLBACK_WIDTH, PREVIEW_FALLBACK_HEIGHT)
    val (rows, columns) = wallpaperResolution.toRowsCols(cellSize)
    return PreviewGeometry(
        bitmapResolution = wallpaperResolution,
        rows = rows,
        columns = columns,
    )
}

private data class PreviewBuffer(
    val index: Int,
    val bitmap: Bitmap,
    val image: ImageBitmap,
)

private data class PreviewRenderRequest(
    val background: Int,
    val layers: List<RenderLayer>,
    val overlays: DeviceOverlays,
)

private class PreviewBufferPool(val resolution: Resolution) {
    private val lock = Any()
    private val buffers = List(PREVIEW_BUFFER_COUNT) { index ->
        val bitmap = Bitmap.createBitmap(resolution.width, resolution.height, Bitmap.Config.ARGB_8888)
        PreviewBuffer(index = index, bitmap = bitmap, image = bitmap.asImageBitmap())
    }
    private val renderingIndices = mutableSetOf<Int>()

    @Suppress("AvoidVarsExceptWithDelegate")
    private var displayedIndex: Int? = null

    @Suppress("AvoidVarsExceptWithDelegate")
    private var pendingFrame: PreviewFrame? = null

    fun acquire(): PreviewBuffer? = synchronized(lock) {
        if (pendingFrame != null || renderingIndices.isNotEmpty()) return@synchronized null
        buffers.firstOrNull { buffer ->
            buffer.index != displayedIndex && buffer.index !in renderingIndices
        }?.also { buffer -> renderingIndices += buffer.index }
    }

    fun publish(buffer: PreviewBuffer, frameId: Long): PreviewFrame = synchronized(lock) {
        check(renderingIndices.remove(buffer.index)) { "Preview buffer was not acquired" }
        PreviewFrame(id = frameId, image = buffer.image, bufferIndex = buffer.index)
            .also { frame -> pendingFrame = frame }
    }

    fun release(buffer: PreviewBuffer) {
        synchronized(lock) { renderingIndices.remove(buffer.index) }
    }

    fun markPresented(frameId: Long): Boolean = synchronized(lock) {
        val frame = pendingFrame?.takeIf { pending -> pending.id == frameId }
            ?: return@synchronized false
        displayedIndex = frame.bufferIndex
        pendingFrame = null
        true
    }
}
