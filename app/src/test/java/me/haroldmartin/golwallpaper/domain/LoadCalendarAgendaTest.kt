package me.haroldmartin.golwallpaper.domain

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking

class LoadCalendarAgendaTest {
    private val instant = Instant.parse("2026-07-11T17:00:00Z")
    private val zone = ZoneId.of("America/Los_Angeles")

    @Test
    fun `permission failure is returned without querying calendars`() {
        runBlocking {
            val repository = FakeCalendarRepository(hasPermission = false)
            val loader = loader(repository)

            assertEquals(
                CalendarAgendaResult.PermissionMissing,
                loader(CalendarOverlaySettings(isEnabled = true, selectedCalendarIds = setOf(1))),
            )
            assertEquals(0, repository.calendarQueries.get())
        }
    }

    @Test
    fun `missing selected source fails closed`() {
        runBlocking {
            val repository = FakeCalendarRepository(
                calendars = listOf(
                    CalendarSource(
                        id = 2,
                        displayName = "Other",
                        accountName = "account",
                        isPrimary = true,
                    ),
                ),
            )

            assertEquals(
                CalendarAgendaResult.SourcesUnavailable,
                loader(repository)(
                    CalendarOverlaySettings(isEnabled = true, selectedCalendarIds = setOf(1)),
                ),
            )
            assertEquals(0, repository.occurrenceQueries.get())
        }
    }

    @Test
    fun `valid source produces an agenda without persisting event details`() {
        runBlocking {
            val repository = FakeCalendarRepository(
                calendars = listOf(
                    CalendarSource(
                        id = 1,
                        displayName = "Primary",
                        accountName = "account",
                        isPrimary = true,
                    ),
                ),
                occurrences = listOf(
                    RawCalendarOccurrence(
                        eventId = 7,
                        calendarId = 1,
                        startMillis = instant.plusSeconds(60).toEpochMilli(),
                        endMillis = instant.plusSeconds(3600).toEpochMilli(),
                        isAllDay = false,
                        title = "Planning",
                        isPrivate = false,
                        isCancelled = false,
                        isDeclined = false,
                    ),
                ),
            )

            val result = loader(repository)(
                CalendarOverlaySettings(isEnabled = true, selectedCalendarIds = setOf(1)),
            )

            val available = assertIs<CalendarAgendaResult.Available>(result)
            val event = available.agenda.days
                .single()
                .events
                .single()
            assertEquals(7L, event.eventId)
        }
    }

    private fun loader(repository: CalendarRepository) = LoadCalendarAgenda(
        repository = repository,
        clock = Clock.fixed(instant, zone),
        zoneId = { zone },
    )
}

private class FakeCalendarRepository(
    private val hasPermission: Boolean = true,
    private val calendars: List<CalendarSource> = emptyList(),
    private val occurrences: List<RawCalendarOccurrence> = emptyList(),
) : CalendarRepository {
    val calendarQueries = AtomicInteger()
    val occurrenceQueries = AtomicInteger()

    override fun hasPermission(): Boolean = hasPermission

    override suspend fun getCalendars(): List<CalendarSource> {
        calendarQueries.incrementAndGet()
        return calendars
    }

    override suspend fun getOccurrences(
        selectedCalendarIds: Set<Long>,
        startMillis: Long,
        endMillis: Long,
    ): List<RawCalendarOccurrence> {
        occurrenceQueries.incrementAndGet()
        return occurrences
    }

    override fun observeChanges(): Flow<Unit> = emptyFlow()
}
