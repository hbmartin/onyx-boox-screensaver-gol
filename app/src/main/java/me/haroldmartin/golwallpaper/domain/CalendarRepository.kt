package me.haroldmartin.golwallpaper.domain

import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun hasPermission(): Boolean
    suspend fun getCalendars(): List<CalendarSource>
    suspend fun getOccurrences(
        selectedCalendarIds: Set<Long>,
        startMillis: Long,
        endMillis: Long,
    ): List<RawCalendarOccurrence>
    fun observeChanges(): Flow<Unit>
}

interface CalendarSettingsStore {
    val settings: Flow<CalendarOverlaySettings>
    suspend fun save(settings: CalendarOverlaySettings)
}

sealed interface CalendarAgendaResult {
    data class Available(val agenda: CalendarAgenda) : CalendarAgendaResult
    data object PermissionMissing : CalendarAgendaResult
    data object SourcesUnavailable : CalendarAgendaResult
}
