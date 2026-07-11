package me.haroldmartin.golwallpaper

import android.content.Context
import me.haroldmartin.golwallpaper.data.AndroidCalendarRepository
import me.haroldmartin.golwallpaper.data.CalendarPreferences
import me.haroldmartin.golwallpaper.data.ObserveUiStateImpl
import me.haroldmartin.golwallpaper.data.SaveBgColorImpl
import me.haroldmartin.golwallpaper.data.SaveFgColorImpl
import me.haroldmartin.golwallpaper.data.SaveLayersImpl
import me.haroldmartin.golwallpaper.data.SaveSettingsImpl
import me.haroldmartin.golwallpaper.data.UserDataStore
import me.haroldmartin.golwallpaper.domain.LoadCalendarAgenda
import me.haroldmartin.golwallpaper.utils.SaveScreensaver
import java.time.Clock
import java.time.ZoneId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Suppress("LateinitUsage", "AvoidVarsExceptWithDelegate", "InjectDispatcher")
object AppContainer {
    lateinit var userDataStore: UserDataStore
        private set
    lateinit var calendarPreferences: CalendarPreferences
        private set
    lateinit var calendarRepository: AndroidCalendarRepository
        private set
    lateinit var applicationContext: Context
        private set

    private val APPLICATION_SCOPE = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val loadCalendarAgenda get() = LoadCalendarAgenda(
        repository = calendarRepository,
        clock = Clock.systemUTC(),
        zoneId = ZoneId::systemDefault,
    )
    val observeUiState get() = ObserveUiStateImpl(
        dataStore = userDataStore,
        calendarPreferences = calendarPreferences,
        defaultDispatcher = defaultDispatcher(),
    )
    val saveBgColor get() = SaveBgColorImpl(userDataStore)
    val saveFgColor get() = SaveFgColorImpl(userDataStore)
    val saveLayers get() = SaveLayersImpl(userDataStore)
    val saveSettings get() = SaveSettingsImpl(userDataStore)
    val saveScreensaver get() = SaveScreensaver(
        dataStore = userDataStore,
        calendarPreferences = calendarPreferences,
        loadCalendarAgenda = loadCalendarAgenda,
        ioDispatcher = Dispatchers.IO,
    )

    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    fun init(context: Context) {
        if (!::userDataStore.isInitialized) {
            applicationContext = context.applicationContext
            userDataStore = UserDataStore(context = applicationContext)
            calendarPreferences = CalendarPreferences(
                context = applicationContext,
                scope = APPLICATION_SCOPE,
            )
            calendarRepository = AndroidCalendarRepository(
                context = applicationContext,
                ioDispatcher = Dispatchers.IO,
            )
        }
    }
}
