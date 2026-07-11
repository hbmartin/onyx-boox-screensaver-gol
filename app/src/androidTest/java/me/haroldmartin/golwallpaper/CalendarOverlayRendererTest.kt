package me.haroldmartin.golwallpaper

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.haroldmartin.golwallpaper.domain.CalendarAgenda
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.OverlayCorner
import me.haroldmartin.golwallpaper.domain.OverlaySize
import me.haroldmartin.golwallpaper.utils.StatsOverlayPosition
import me.haroldmartin.golwallpaper.utils.drawCalendarOverlay
import me.haroldmartin.golwallpaper.utils.drawStatsOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CalendarOverlayRendererTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val agenda = CalendarAgenda(
        today = LocalDate.of(2026, 7, 11),
        days = emptyList(),
        providedTitles = emptyMap(),
        overflowCount = 0,
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
                agenda = agenda,
                settings = CalendarOverlaySettings(isEnabled = true, size = size),
                backgroundColor = Color.WHITE,
            )
            assertEquals(width, bounds.width(), 0.1f)
            bitmap.recycle()
        }
    }

    @Test
    fun everyCornerStaysInsideBitmap() {
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
}
