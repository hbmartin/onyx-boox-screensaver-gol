package me.haroldmartin.golwallpaper.domain

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
    fun `valid source produces an agenda with a provided title lookup`() {
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
            assertEquals("Planning", available.agenda.providedTitles[event.eventId])
        }
    }

    @Test
    fun `calendar query failure returns sources unavailable`() {
        runBlocking {
            val repository = FakeCalendarRepository(
                calendarFailure = IllegalStateException("calendar provider unavailable"),
            )

            assertEquals(
                CalendarAgendaResult.SourcesUnavailable,
                loader(repository)(
                    CalendarOverlaySettings(isEnabled = true, selectedCalendarIds = setOf(1)),
                ),
            )
        }
    }

    @Test
    fun `occurrence query failure returns sources unavailable`() {
        runBlocking {
            val repository = FakeCalendarRepository(
                calendars = listOf(primaryCalendar()),
                occurrenceFailure = IllegalStateException("instance query unavailable"),
            )

            assertEquals(
                CalendarAgendaResult.SourcesUnavailable,
                loader(repository)(
                    CalendarOverlaySettings(isEnabled = true, selectedCalendarIds = setOf(1)),
                ),
            )
        }
    }

    @Test
    fun `cancellation during occurrence query is rethrown`() {
        runBlocking {
            val repository = FakeCalendarRepository(
                calendars = listOf(primaryCalendar()),
                cancelOccurrenceQuery = true,
            )

            // A coroutine whose job is cancelled can never complete normally, so awaiting
            // alone cannot tell a rethrown cancellation from a swallowed one; the flag can.
            val completedNormally = AtomicBoolean(false)
            val result = async {
                val outcome = loader(repository)(
                    CalendarOverlaySettings(isEnabled = true, selectedCalendarIds = setOf(1)),
                )
                completedNormally.set(true)
                outcome
            }

            assertFailsWith<CancellationException> { result.await() }
            assertFalse(completedNormally.get())
        }
    }

    private fun loader(repository: CalendarRepository) = LoadCalendarAgenda(
        repository = repository,
        clock = Clock.fixed(instant, zone),
        zoneId = { zone },
    )

    private fun primaryCalendar() = CalendarSource(
        id = 1,
        displayName = "Primary",
        accountName = "account",
        isPrimary = true,
    )
}

private class FakeCalendarRepository(
    private val hasPermission: Boolean = true,
    private val calendars: List<CalendarSource> = emptyList(),
    private val occurrences: List<RawCalendarOccurrence> = emptyList(),
    private val calendarFailure: Exception? = null,
    private val occurrenceFailure: Exception? = null,
    private val cancelOccurrenceQuery: Boolean = false,
) : CalendarRepository {
    val calendarQueries = AtomicInteger()
    val occurrenceQueries = AtomicInteger()

    override fun hasPermission(): Boolean = hasPermission

    override suspend fun getCalendars(): List<CalendarSource> {
        calendarQueries.incrementAndGet()
        calendarFailure?.let { throw it }
        return calendars
    }

    override suspend fun getOccurrences(
        selectedCalendarIds: Set<Long>,
        startMillis: Long,
        endMillis: Long,
    ): List<RawCalendarOccurrence> {
        occurrenceQueries.incrementAndGet()
        if (cancelOccurrenceQuery) {
            currentCoroutineContext().cancel(CancellationException("calendar query cancelled"))
            currentCoroutineContext().ensureActive()
        }
        occurrenceFailure?.let { throw it }
        return occurrences
    }

    override fun observeChanges(): Flow<Unit> = emptyFlow()
}
