package me.haroldmartin.golwallpaper.data

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import me.haroldmartin.golwallpaper.domain.CalendarRepository
import me.haroldmartin.golwallpaper.domain.CalendarSource
import me.haroldmartin.golwallpaper.domain.RawCalendarOccurrence
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class AndroidCalendarRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) : CalendarRepository {
    private val appContext = context.applicationContext
    private val resolver: ContentResolver = appContext.contentResolver

    override fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        appContext,
        Manifest.permission.READ_CALENDAR,
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    override suspend fun getCalendars(): List<CalendarSource> = withContext(ioDispatcher) {
        if (!hasPermission()) return@withContext emptyList()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val selection = buildString {
            append("${CalendarContract.Calendars.VISIBLE} = 1")
            append(" AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1")
            append(" AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?")
        }
        resolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            arrayOf(CalendarContract.Calendars.CAL_ACCESS_READ.toString()),
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            )
            val accountIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            val primaryIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CalendarSource(
                            id = cursor.getLong(idIndex),
                            displayName = cursor.getString(nameIndex).orEmpty(),
                            accountName = cursor.getString(accountIndex).orEmpty(),
                            isPrimary = cursor.getInt(primaryIndex) == 1,
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    @Suppress("LongMethod")
    override suspend fun getOccurrences(
        selectedCalendarIds: Set<Long>,
        startMillis: Long,
        endMillis: Long,
    ): List<RawCalendarOccurrence> = withContext(ioDispatcher) {
        if (!hasPermission() || selectedCalendarIds.isEmpty()) return@withContext emptyList()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also { builder ->
            ContentUris.appendId(builder, startMillis)
            ContentUris.appendId(builder, endMillis)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ACCESS_LEVEL,
            CalendarContract.Instances.STATUS,
            CalendarContract.Instances.SELF_ATTENDEE_STATUS,
        )
        resolver.query(
            uri,
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN,
        )?.use { cursor ->
            val eventIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val calendarIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
            val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val accessIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ACCESS_LEVEL)
            val statusIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.STATUS)
            val attendeeIndex = cursor.getColumnIndexOrThrow(
                CalendarContract.Instances.SELF_ATTENDEE_STATUS,
            )
            buildList {
                while (cursor.moveToNext()) {
                    val calendarId = cursor.getLong(calendarIndex)
                    if (calendarId !in selectedCalendarIds) continue
                    val access = cursor.getInt(accessIndex)
                    add(
                        RawCalendarOccurrence(
                            eventId = cursor.getLong(eventIndex),
                            calendarId = calendarId,
                            startMillis = cursor.getLong(beginIndex),
                            endMillis = cursor.getLong(endIndex),
                            isAllDay = cursor.getInt(allDayIndex) == 1,
                            title = cursor.getString(titleIndex),
                            isPrivate = access == CalendarContract.Events.ACCESS_PRIVATE ||
                                access == CalendarContract.Events.ACCESS_CONFIDENTIAL,
                            isCancelled = cursor.getInt(statusIndex) ==
                                CalendarContract.Events.STATUS_CANCELED,
                            isDeclined = cursor.getInt(attendeeIndex) ==
                                CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED,
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    override fun observeChanges(): Flow<Unit> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, observer)
        resolver.registerContentObserver(CalendarContract.Calendars.CONTENT_URI, true, observer)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
}
