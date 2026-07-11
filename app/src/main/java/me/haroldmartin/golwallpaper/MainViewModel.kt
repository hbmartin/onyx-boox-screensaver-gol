package me.haroldmartin.golwallpaper

import android.content.Context
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
import me.haroldmartin.golwallpaper.utils.drawCalendarOverlay
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5000L
private const val PREVIEW_DELAY_MILLIS = 500L
private const val PREVIEW_WIDTH = 360
private const val PREVIEW_HEIGHT = 480
private const val PREVIEW_ROWS = 48
private const val PREVIEW_COLUMNS = 36
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
    private val _previewImage = MutableStateFlow<ImageBitmap?>(null)
    val previewImage: StateFlow<ImageBitmap?> = _previewImage.asStateFlow()
    private val previewPlayingMutable = MutableStateFlow(false)
    val previewPlaying: StateFlow<Boolean> = previewPlayingMutable.asStateFlow()
    private val previewAgenda = MutableStateFlow<CalendarAgenda?>(null)
    private val _calendarUiState = MutableStateFlow(CalendarUiState())
    val calendarUiState: StateFlow<CalendarUiState> = _calendarUiState.asStateFlow()

    @Suppress("AvoidVarsExceptWithDelegate")
    private var previewRenderJob: Job? = null

    @Suppress("AvoidVarsExceptWithDelegate")
    private var calendarObservationJob: Job? = null

    init {
        viewModelScope.launch {
            previewPlayingMutable.collectLatest { isPlaying ->
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

    fun playPreview() {
        previewPlayingMutable.value = true
    }

    fun pausePreview() {
        previewPlayingMutable.value = false
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
        refreshCalendarAgenda()
        renderPreview()
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
        previewAgenda.value = null
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
        previewLayers.value = previewLayers.value.onEach { previewLayer ->
            if (previewLayer.isEnabled) previewLayer.controller.update()
        }
    }

    private fun renderPreview() {
        val background = previewBackground.value
        val agenda = previewAgenda.value
        val calendarSettings = uiState.value.calendarOverlaySettings
        val layers = previewLayers.value.filter(PreviewLayer::isEnabled).map { previewLayer ->
            RenderLayer(
                grid = Array(previewLayer.controller.grid.size) { row ->
                    previewLayer.controller.grid[row].clone()
                },
                fgColor = previewLayer.fgColor,
            )
        }
        previewRenderJob?.cancel()
        previewRenderJob = viewModelScope.launch(renderDispatcher) {
            val bitmap = createCompositeBitmap(
                width = PREVIEW_WIDTH,
                height = PREVIEW_HEIGHT,
                backgroundColor = background,
                layers = layers,
            )
            if (agenda != null) {
                drawCalendarOverlay(
                    context = appContext,
                    bitmap = bitmap,
                    agenda = agenda,
                    settings = calendarSettings,
                    backgroundColor = background,
                )
            }
            if (currentCoroutineContext().isActive) {
                _previewImage.value = bitmap.asImageBitmap()
            } else {
                bitmap.recycle()
            }
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

    private fun Layer.toPreviewLayer(background: Int): PreviewLayer = PreviewLayer(
        controller = previewController(state = state, rule = rule),
        fgColor = resolvePreviewColor(fgColor, except = setOf(background)),
        isEnabled = isEnabled,
    )

    @Suppress("SwallowedException")
    private fun previewController(state: String?, rule: String): GolController = try {
        if (state == null) {
            GolController(
                rows = PREVIEW_ROWS,
                columns = PREVIEW_COLUMNS,
                initialPattern = ".",
                rule = rule,
            ).apply { reset(pattern = null) }
        } else {
            GolController(
                rows = PREVIEW_ROWS,
                columns = PREVIEW_COLUMNS,
                initialPattern = state.toPreviewPattern(),
                rule = rule,
            )
        }
    } catch (exception: IllegalArgumentException) {
        GolController(
            rows = PREVIEW_ROWS,
            columns = PREVIEW_COLUMNS,
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
