package me.haroldmartin.golwallpaper

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.haroldmartin.golwallpaper.domain.AgendaDay
import me.haroldmartin.golwallpaper.domain.AgendaEvent
import me.haroldmartin.golwallpaper.domain.AgendaTitle
import me.haroldmartin.golwallpaper.domain.CalendarAgenda
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.OverlayCorner
import me.haroldmartin.golwallpaper.domain.OverlaySize
import me.haroldmartin.golwallpaper.utils.StatsOverlayPosition
import me.haroldmartin.golwallpaper.utils.DeviceOverlays
import me.haroldmartin.golwallpaper.utils.RenderLayer
import me.haroldmartin.golwallpaper.utils.RenderStats
import me.haroldmartin.golwallpaper.utils.drawCalendarOverlay
import me.haroldmartin.golwallpaper.utils.drawStatsOverlay
import me.haroldmartin.golwallpaper.utils.renderDeviceBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CalendarOverlayRendererTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val today = LocalDate.of(2026, 7, 11)
    private val emptyAgenda = CalendarAgenda(
        today = today,
        days = emptyList(),
        providedTitles = emptyMap(),
        overflowCount = 0,
    )
    private val populatedAgenda = CalendarAgenda(
        today = today,
        days = (0L..2L).map { offset ->
            val date = today.plusDays(offset)
            AgendaDay(
                date = date,
                events = listOf(
                    agendaEvent(eventId = offset * 2, date = date, title = AgendaTitle.BUSY),
                    agendaEvent(eventId = offset * 2 + 1, date = date, title = AgendaTitle.UNTITLED),
                ),
            )
        },
        providedTitles = emptyMap(),
        overflowCount = 2,
    )

    @Test
    fun cardSizesUseConfiguredWidths() {
        val expected = mapOf(
            OverlaySize.SMALL to 550f,
            OverlaySize.MEDIUM to 700f,
            OverlaySize.LARGE to 850f,
        )

        expected.forEach { (size, width) ->
            val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
            val bounds = drawCalendarOverlay(
                context = context,
                bitmap = bitmap,
                agenda = emptyAgenda,
                settings = CalendarOverlaySettings(isEnabled = true, size = size),
                backgroundColor = Color.WHITE,
            )
            assertEquals(width, bounds.width(), 0.1f)
            bitmap.recycle()
        }
    }

    @Test
    fun everyCornerStaysInsideBitmap() {
        listOf(emptyAgenda, populatedAgenda).forEach { agenda ->
            OverlayCorner.entries.forEach { corner ->
                val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
                val bounds = drawCalendarOverlay(
                    context = context,
                    bitmap = bitmap,
                    agenda = agenda,
                    settings = CalendarOverlaySettings(isEnabled = true, corner = corner),
                    backgroundColor = Color.WHITE,
                )
                assertTrue(bounds.left >= 0f)
                assertTrue(bounds.top >= 0f)
                assertTrue(bounds.right <= bitmap.width)
                assertTrue(bounds.bottom <= bitmap.height)
                bitmap.recycle()
            }
        }
    }

    @Test
    fun bottomRightStatsDoNotPaintBottomLeft() {
        val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)

        drawStatsOverlay(
            bitmap = bitmap,
            text = "Gen 1",
            textColor = Color.BLACK,
            backgroundColor = Color.WHITE,
            position = StatsOverlayPosition.BOTTOM_RIGHT,
        )

        assertEquals(Color.RED, bitmap.getPixel(0, 999))
        assertEquals(Color.WHITE, bitmap.getPixel(999, 999))
        bitmap.recycle()
    }

    @Test
    fun sharedDeviceRendererMovesStatsAwayFromBottomLeftCalendar() {
        val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)

        renderDeviceBitmap(
            context = context,
            bitmap = bitmap,
            backgroundColor = Color.WHITE,
            layers = listOf(
                RenderLayer(
                    grid = arrayOf(booleanArrayOf(true)),
                    fgColor = Color.BLACK,
                ),
            ),
            overlays = DeviceOverlays(
                calendarAgenda = emptyAgenda,
                calendarSettings = CalendarOverlaySettings(
                    isEnabled = true,
                    corner = OverlayCorner.BOTTOM_LEFT,
                ),
                stats = RenderStats(generation = 4, population = 12),
            ),
        )

        assertEquals(Color.BLACK, bitmap.getPixel(0, 999))
        assertEquals(Color.WHITE, bitmap.getPixel(999, 999))
        bitmap.recycle()
    }

    private fun agendaEvent(eventId: Long, date: LocalDate, title: AgendaTitle) = AgendaEvent(
        eventId = eventId,
        calendarId = 1,
        date = date,
        startMillis = 0,
        isAllDay = false,
        title = title,
    )
}
