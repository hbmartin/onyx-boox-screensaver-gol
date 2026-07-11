package me.haroldmartin.golwallpaper.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("LongParameterList", "NamedArguments")
class CalendarOverlayTest {
    private val zone = ZoneId.of("America/Los_Angeles")
    private val now = Instant.parse("2026-07-11T17:00:00Z")

    @Test
    fun `settings use privacy-safe defaults`() {
        val settings = CalendarOverlaySettings()

        assertFalse(settings.isEnabled)
        assertEquals(CalendarHorizon.NEXT_7_DAYS, settings.horizon)
        assertEquals(OverlayCorner.TOP_RIGHT, settings.corner)
        assertEquals(OverlaySize.MEDIUM, settings.size)
        assertTrue(settings.selectedCalendarIds.isEmpty())
    }

    @Test
    fun `seven day window ends at local midnight after seven calendar dates`() {
        val window = calendarTimeWindow(now, zone, CalendarHorizon.NEXT_7_DAYS)

        assertEquals(
            LocalDate.of(2026, 7, 11).atStartOfDay(zone).toInstant().toEpochMilli(),
            window.queryStartMillis,
        )
        assertEquals(
            LocalDate.of(2026, 7, 18).atStartOfDay(zone).toInstant().toEpochMilli(),
            window.endMillis,
        )
    }

    @Test
    fun `rolling 24 hour window remains 24 hours over daylight saving change`() {
        val beforeFallback = Instant.parse("2026-11-01T07:30:00Z")
        val window = calendarTimeWindow(beforeFallback, zone, CalendarHorizon.NEXT_24_HOURS)

        assertEquals(24L * 60L * 60L * 1000L, window.endMillis - beforeFallback.toEpochMilli())
    }

    @Test
    fun `agenda filters started cancelled and declined timed events`() {
        val end = calendarTimeWindow(now, zone, CalendarHorizon.NEXT_24_HOURS).endMillis
        val agenda = buildCalendarAgenda(
            occurrences = listOf(
                timed(1, now.minusSeconds(60), title = "Started"),
                timed(2, now.plusSeconds(60), title = "Cancelled", isCancelled = true),
                timed(3, now.plusSeconds(120), title = "Declined", isDeclined = true),
                timed(4, now.plusSeconds(180), title = "Visible"),
            ),
            now = now,
            zoneId = zone,
            endMillis = end,
        )

        assertEquals(listOf(4L), agenda.days.flatMap(AgendaDay::events).map(AgendaEvent::eventId))
    }

    @Test
    fun `agenda orders all day before timed and masks titles`() {
        val todayUtc = LocalDate.of(2026, 7, 11).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = calendarTimeWindow(now, zone, CalendarHorizon.TODAY).endMillis
        val agenda = buildCalendarAgenda(
            occurrences = listOf(
                timed(1, now.plusSeconds(120), title = "Private", isPrivate = true),
                timed(2, now.plusSeconds(60), title = "  "),
                allDay(3, todayUtc, todayUtc.plusSeconds(DAY_SECONDS), "Holiday"),
            ),
            now = now,
            zoneId = zone,
            endMillis = end,
        )
        val events = agenda.days.single().events

        assertEquals(listOf(3L, 2L, 1L), events.map(AgendaEvent::eventId))
        assertEquals(AgendaTitle.PROVIDED, events[0].title)
        assertEquals(AgendaTitle.UNTITLED, events[1].title)
        assertEquals(AgendaTitle.BUSY, events[2].title)
    }

    @Test
    fun `multi day all day events repeat and count toward overflow`() {
        val start = LocalDate.of(2026, 7, 10).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = LocalDate.of(2026, 7, 18).atStartOfDay(ZoneOffset.UTC).toInstant()
        val agenda = buildCalendarAgenda(
            occurrences = listOf(allDay(1, start, end, "Conference")),
            now = now,
            zoneId = zone,
            endMillis = calendarTimeWindow(now, zone, CalendarHorizon.NEXT_7_DAYS).endMillis,
        )

        assertEquals(MAX_CALENDAR_EVENTS, agenda.days.sumOf { day -> day.events.size })
        assertEquals(2, agenda.overflowCount)
        assertEquals(5, agenda.days.size)
    }

    @Test
    fun `provided titles are keyed per occurrence date for multi day events`() {
        val start = LocalDate.of(2026, 7, 11).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = LocalDate.of(2026, 7, 14).atStartOfDay(ZoneOffset.UTC).toInstant()
        val agenda = buildCalendarAgenda(
            occurrences = listOf(allDay(1, start, end, "Conference")),
            now = now,
            zoneId = zone,
            endMillis = calendarTimeWindow(now, zone, CalendarHorizon.NEXT_7_DAYS).endMillis,
        )
        val events = agenda.days.flatMap(AgendaDay::events)

        assertEquals(3, events.size)
        events.forEach { event -> assertEquals("Conference", agenda.providedTitles[event.key]) }
        // Distinct keys per date so overlapping occurrences never share a title slot.
        assertEquals(events.size, events.map(AgendaEvent::key).toSet().size)
    }

    @Test
    fun `duplicate copies from separate calendars are retained`() {
        val start = now.plusSeconds(60)
        val end = calendarTimeWindow(now, zone, CalendarHorizon.TODAY).endMillis
        val agenda = buildCalendarAgenda(
            occurrences = listOf(
                timed(1, start, calendarId = 10, title = "Standup"),
                timed(2, start, calendarId = 20, title = "Standup"),
            ),
            now = now,
            zoneId = zone,
            endMillis = end,
        )

        assertEquals(2, agenda.days.single().events.size)
    }

    @Test
    fun `selection retains valid stored calendars then falls back to primary or all`() {
        val sources = listOf(
            CalendarSource(1, "One", "a@example.com", isPrimary = false),
            CalendarSource(2, "Two", "b@example.com", isPrimary = true),
        )

        assertEquals(setOf(1L), initialCalendarSelection(sources, setOf(1L, 99L)))
        assertEquals(setOf(2L), initialCalendarSelection(sources, emptySet()))
        assertEquals(
            setOf(1L, 2L),
            initialCalendarSelection(sources.map { it.copy(isPrimary = false) }, emptySet()),
        )
    }

    private fun timed(
        id: Long,
        start: Instant,
        calendarId: Long = 1,
        title: String?,
        isPrivate: Boolean = false,
        isCancelled: Boolean = false,
        isDeclined: Boolean = false,
    ) = RawCalendarOccurrence(
        eventId = id,
        calendarId = calendarId,
        startMillis = start.toEpochMilli(),
        endMillis = start.plusSeconds(1800).toEpochMilli(),
        isAllDay = false,
        title = title,
        isPrivate = isPrivate,
        isCancelled = isCancelled,
        isDeclined = isDeclined,
    )

    private fun allDay(id: Long, start: Instant, end: Instant, title: String) =
        RawCalendarOccurrence(
            eventId = id,
            calendarId = 1,
            startMillis = start.toEpochMilli(),
            endMillis = end.toEpochMilli(),
            isAllDay = true,
            title = title,
            isPrivate = false,
            isCancelled = false,
            isDeclined = false,
        )

    private companion object {
        const val DAY_SECONDS = 86_400L
    }
}
