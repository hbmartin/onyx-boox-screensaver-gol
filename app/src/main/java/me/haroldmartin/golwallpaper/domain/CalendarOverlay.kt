package me.haroldmartin.golwallpaper.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

const val MAX_CALENDAR_EVENTS = 5
private const val HOURS_PER_DAY = 24L
private const val DAYS_IN_HORIZON = 7L

enum class CalendarHorizon {
    TODAY,
    NEXT_24_HOURS,
    NEXT_7_DAYS,
}

enum class OverlayCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
}

enum class OverlaySize {
    SMALL,
    MEDIUM,
    LARGE,
}

data class CalendarOverlaySettings(
    val isEnabled: Boolean = false,
    val horizon: CalendarHorizon = CalendarHorizon.NEXT_7_DAYS,
    val corner: OverlayCorner = OverlayCorner.TOP_RIGHT,
    val size: OverlaySize = OverlaySize.MEDIUM,
    val selectedCalendarIds: Set<Long> = emptySet(),
)

data class CalendarSource(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val isPrimary: Boolean,
)

fun initialCalendarSelection(
    sources: List<CalendarSource>,
    storedIds: Set<Long>,
): Set<Long> {
    val eligibleIds = sources.map(CalendarSource::id).toSet()
    val retainedIds = storedIds intersect eligibleIds
    if (retainedIds.isNotEmpty()) return retainedIds
    return sources.filter(CalendarSource::isPrimary).map(CalendarSource::id).toSet()
        .ifEmpty { eligibleIds }
}

data class RawCalendarOccurrence(
    val eventId: Long,
    val calendarId: Long,
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
    val title: String?,
    val isPrivate: Boolean,
    val isCancelled: Boolean,
    val isDeclined: Boolean,
)

data class AgendaEvent(
    val eventId: Long,
    val calendarId: Long,
    val date: LocalDate,
    val startMillis: Long,
    val isAllDay: Boolean,
    val title: AgendaTitle,
) {
    val key: AgendaEventKey get() = AgendaEventKey(eventId = eventId, calendarId = calendarId, date = date)
}

// Stable identity for an agenda entry, independent of rendering-derived fields. A multi-day
// all-day event yields one key per date, so provided titles map to the correct occurrence.
data class AgendaEventKey(
    val eventId: Long,
    val calendarId: Long,
    val date: LocalDate,
)

enum class AgendaTitle {
    BUSY,
    UNTITLED,
    PROVIDED,
}

data class AgendaDay(
    val date: LocalDate,
    val events: List<AgendaEvent>,
)

data class CalendarAgenda(
    val today: LocalDate,
    val days: List<AgendaDay>,
    val providedTitles: Map<Long, String>,
    val overflowCount: Int,
)

data class CalendarTimeWindow(
    val queryStartMillis: Long,
    val endMillis: Long,
)

fun calendarTimeWindow(
    now: Instant,
    zoneId: ZoneId,
    horizon: CalendarHorizon,
): CalendarTimeWindow {
    val today = now.atZone(zoneId).toLocalDate()
    val startOfToday = today.atStartOfDay(zoneId).toInstant()
    val end = when (horizon) {
        CalendarHorizon.TODAY -> today.plusDays(1).atStartOfDay(zoneId).toInstant()
        CalendarHorizon.NEXT_24_HOURS -> now.plus(Duration.ofHours(HOURS_PER_DAY))
        CalendarHorizon.NEXT_7_DAYS -> today.plusDays(DAYS_IN_HORIZON)
            .atStartOfDay(zoneId)
            .toInstant()
    }
    return CalendarTimeWindow(
        queryStartMillis = startOfToday.toEpochMilli(),
        endMillis = end.toEpochMilli(),
    )
}

fun buildCalendarAgenda(
    occurrences: List<RawCalendarOccurrence>,
    now: Instant,
    zoneId: ZoneId,
    endMillis: Long,
): CalendarAgenda {
    val today = now.atZone(zoneId).toLocalDate()
    val expanded = occurrences
        .asSequence()
        .filterNot { occurrence -> occurrence.isCancelled || occurrence.isDeclined }
        .flatMap { occurrence ->
            if (occurrence.isAllDay) {
                expandAllDayOccurrence(
                    occurrence = occurrence,
                    today = today,
                    zoneId = zoneId,
                    endMillis = endMillis,
                )
            } else {
                expandTimedOccurrence(
                    occurrence = occurrence,
                    nowMillis = now.toEpochMilli(),
                    zoneId = zoneId,
                    endMillis = endMillis,
                )
            }
        }
        .sortedWith(
            compareBy<AgendaEvent>(AgendaEvent::date)
                .thenByDescending(AgendaEvent::isAllDay)
                .thenBy(AgendaEvent::startMillis),
        )
        .toList()
    val visible = expanded.take(MAX_CALENDAR_EVENTS)
    val titles = visible.mapNotNull { event ->
        val rawTitle = occurrences.firstOrNull { occurrence ->
            occurrence.eventId == event.eventId && occurrence.calendarId == event.calendarId
        }?.run { title?.trim() }
        if (event.title == AgendaTitle.PROVIDED && !rawTitle.isNullOrEmpty()) event.eventId to rawTitle else null
    }.toMap()

    return CalendarAgenda(
        today = today,
        days = visible.groupBy(AgendaEvent::date).map { (date, events) ->
            AgendaDay(date = date, events = events)
        },
        providedTitles = titles,
        overflowCount = (expanded.size - visible.size).coerceAtLeast(0),
    )
}

private fun expandTimedOccurrence(
    occurrence: RawCalendarOccurrence,
    nowMillis: Long,
    zoneId: ZoneId,
    endMillis: Long,
): Sequence<AgendaEvent> {
    if (occurrence.startMillis <= nowMillis || occurrence.startMillis >= endMillis) return emptySequence()
    return sequenceOf(occurrence.toAgendaEvent(occurrence.startMillis.toLocalDate(zoneId)))
}

private fun expandAllDayOccurrence(
    occurrence: RawCalendarOccurrence,
    today: LocalDate,
    zoneId: ZoneId,
    endMillis: Long,
): Sequence<AgendaEvent> {
    val eventStart = occurrence.startMillis.toUtcDate()
    val eventEndExclusive = occurrence.endMillis.toUtcDate().coerceAtLeast(eventStart.plusDays(1))
    val horizonEndExclusive = Instant.ofEpochMilli(endMillis).atZone(zoneId).toLocalDate().let { endDate ->
        if (endDate.atStartOfDay(zoneId).toInstant().toEpochMilli() == endMillis) endDate else endDate.plusDays(1)
    }
    val firstDate = maxOf(today, eventStart)
    val lastExclusive = minOf(eventEndExclusive, horizonEndExclusive)
    if (!firstDate.isBefore(lastExclusive)) return emptySequence()
    return generateSequence(firstDate) { date -> date.plusDays(1) }
        .takeWhile { date -> date.isBefore(lastExclusive) }
        .map { date -> occurrence.toAgendaEvent(date) }
}

private fun RawCalendarOccurrence.toAgendaEvent(date: LocalDate): AgendaEvent = AgendaEvent(
    eventId = eventId,
    calendarId = calendarId,
    date = date,
    startMillis = startMillis,
    isAllDay = isAllDay,
    title = when {
        isPrivate -> AgendaTitle.BUSY
        title.isNullOrBlank() -> AgendaTitle.UNTITLED
        else -> AgendaTitle.PROVIDED
    },
)

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

private fun Long.toUtcDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
