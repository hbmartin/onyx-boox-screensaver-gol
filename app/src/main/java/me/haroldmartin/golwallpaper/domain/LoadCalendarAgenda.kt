package me.haroldmartin.golwallpaper.domain

import java.time.Clock
import java.time.ZoneId
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class LoadCalendarAgenda(
    private val repository: CalendarRepository,
    private val clock: Clock,
    private val zoneId: () -> ZoneId,
) {
    @Suppress("TooGenericExceptionCaught")
    suspend operator fun invoke(settings: CalendarOverlaySettings): CalendarAgendaResult {
        return when {
            !settings.isEnabled || settings.selectedCalendarIds.isEmpty() -> {
                CalendarAgendaResult.SourcesUnavailable
            }
            !repository.hasPermission() -> {
                CalendarAgendaResult.PermissionMissing
            }
            else -> {
                loadAvailable(settings)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadAvailable(
        settings: CalendarOverlaySettings,
    ): CalendarAgendaResult = try {
        val eligibleIds = repository.getCalendars().map(CalendarSource::id).toSet()
        val selectedIds = settings.selectedCalendarIds intersect eligibleIds
        if (selectedIds.isEmpty()) {
            CalendarAgendaResult.SourcesUnavailable
        } else {
            val now = clock.instant()
            val currentZone = zoneId()
            val window = calendarTimeWindow(now, currentZone, settings.horizon)
            val occurrences = repository.getOccurrences(
                selectedCalendarIds = selectedIds,
                startMillis = window.queryStartMillis,
                endMillis = window.endMillis,
            )
            CalendarAgendaResult.Available(
                buildCalendarAgenda(
                    occurrences = occurrences,
                    now = now,
                    zoneId = currentZone,
                    endMillis = window.endMillis,
                ),
            )
        }
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        CalendarAgendaResult.SourcesUnavailable
    }
}
