package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.AgendaEvent
import me.haroldmartin.golwallpaper.domain.AgendaTitle
import me.haroldmartin.golwallpaper.domain.CalendarAgenda
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.OverlayCorner
import me.haroldmartin.golwallpaper.domain.OverlaySize
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

private const val SMALL_WIDTH_FRACTION = 0.55f
private const val MEDIUM_WIDTH_FRACTION = 0.70f
private const val LARGE_WIDTH_FRACTION = 0.85f
private const val SMALL_TEXT_FRACTION = 0.018f
private const val MEDIUM_TEXT_FRACTION = 0.023f
private const val LARGE_TEXT_FRACTION = 0.028f
private const val PADDING_MULTIPLIER = 0.6f
private const val LINE_HEIGHT_MULTIPLIER = 1.3f
private const val BORDER_DIVISOR = 12f
private const val MIN_BORDER_WIDTH = 1f
private const val ELLIPSIS_WIDTH_FRACTION = 0.98f
private const val OPAQUE_ALPHA = 0xFF000000.toInt()
private const val RGB_MASK = 0x00FFFFFF

fun drawCalendarOverlay(
    context: Context,
    bitmap: Bitmap,
    agenda: CalendarAgenda,
    settings: CalendarOverlaySettings,
    backgroundColor: Int,
): RectF {
    val bodyTextSize = bitmap.height * settings.size.textFraction
    val padding = bodyTextSize * PADDING_MULTIPLIER
    val margin = bodyTextSize
    val lineHeight = bodyTextSize * LINE_HEIGHT_MULTIPLIER
    val cardWidth = bitmap.width * settings.size.widthFraction
    val lines = agenda.toDisplayLines(context)
    val cardHeight = padding * 2 + lineHeight * lines.size
    val bounds = settings.corner.bounds(
        bitmapWidth = bitmap.width.toFloat(),
        bitmapHeight = bitmap.height.toFloat(),
        cardWidth = cardWidth,
        cardHeight = cardHeight,
        margin = margin,
    )
    val inverseColor = backgroundColor.inverseRgb()
    val canvas = Canvas(bitmap)
    canvas.drawRect(bounds, Paint().apply { color = backgroundColor })
    canvas.drawRect(
        bounds,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inverseColor
            style = Paint.Style.STROKE
            strokeWidth = maxOf(MIN_BORDER_WIDTH, bodyTextSize / BORDER_DIVISOR)
        },
    )

    val regularPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inverseColor
        textSize = bodyTextSize
        typeface = Typeface.SANS_SERIF
    }
    val headingPaint = TextPaint(regularPaint).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val availableWidth = (bounds.width() - padding * 2) * ELLIPSIS_WIDTH_FRACTION
    lines.forEachIndexed { index, line ->
        val paint = if (line.isHeading) headingPaint else regularPaint
        val baseline = bounds.top + padding + lineHeight * index - paint.fontMetrics.ascent
        val text = TextUtils.ellipsize(
            line.text,
            paint,
            availableWidth,
            TextUtils.TruncateAt.END,
        ).toString()
        canvas.drawText(text, bounds.left + padding, baseline, paint)
    }
    return bounds
}

private data class DisplayLine(val text: String, val isHeading: Boolean)

private fun CalendarAgenda.toDisplayLines(context: Context): List<DisplayLine> {
    if (days.isEmpty()) {
        return listOf(
            DisplayLine(context.getString(R.string.calendar_no_events), isHeading = false),
        )
    }
    return buildList {
        days.forEach { day ->
            add(DisplayLine(day.date.displayName(context, today), isHeading = true))
            day.events.forEach { event ->
                add(DisplayLine(event.displayText(context, this@toDisplayLines), isHeading = false))
            }
        }
        if (overflowCount > 0) {
            add(
                DisplayLine(
                    context.resources.getQuantityString(
                        R.plurals.calendar_more_events,
                        overflowCount,
                        overflowCount,
                    ),
                    isHeading = false,
                ),
            )
        }
    }
}

private fun AgendaEvent.displayText(context: Context, agenda: CalendarAgenda): String {
    val time = if (isAllDay) {
        context.getString(R.string.calendar_all_day)
    } else {
        DateFormat.getTimeFormat(context).format(Date(startMillis))
    }
    val displayTitle = when (title) {
        AgendaTitle.BUSY -> context.getString(R.string.calendar_busy)
        AgendaTitle.UNTITLED -> context.getString(R.string.calendar_untitled)
        AgendaTitle.PROVIDED -> agenda.providedTitles[key].orEmpty()
    }
    return context.getString(R.string.calendar_event_format, time, displayTitle)
}

private fun LocalDate.displayName(context: Context, today: LocalDate): String = when (this) {
    today -> context.getString(R.string.calendar_today)
    today.plusDays(1) -> context.getString(R.string.calendar_tomorrow)
    else -> DateUtils.formatDateTime(
        context,
        atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        DateUtils.FORMAT_SHOW_WEEKDAY or
            DateUtils.FORMAT_SHOW_DATE or
            DateUtils.FORMAT_ABBREV_ALL,
    )
}

private val OverlaySize.widthFraction: Float
    get() = when (this) {
        OverlaySize.SMALL -> SMALL_WIDTH_FRACTION
        OverlaySize.MEDIUM -> MEDIUM_WIDTH_FRACTION
        OverlaySize.LARGE -> LARGE_WIDTH_FRACTION
    }

private val OverlaySize.textFraction: Float
    get() = when (this) {
        OverlaySize.SMALL -> SMALL_TEXT_FRACTION
        OverlaySize.MEDIUM -> MEDIUM_TEXT_FRACTION
        OverlaySize.LARGE -> LARGE_TEXT_FRACTION
    }

private fun OverlayCorner.bounds(
    bitmapWidth: Float,
    bitmapHeight: Float,
    cardWidth: Float,
    cardHeight: Float,
    margin: Float,
): RectF {
    val left = when (this) {
        OverlayCorner.TOP_LEFT, OverlayCorner.BOTTOM_LEFT -> margin
        OverlayCorner.TOP_RIGHT, OverlayCorner.BOTTOM_RIGHT -> bitmapWidth - margin - cardWidth
    }
    val top = when (this) {
        OverlayCorner.TOP_LEFT, OverlayCorner.TOP_RIGHT -> margin
        OverlayCorner.BOTTOM_LEFT, OverlayCorner.BOTTOM_RIGHT -> bitmapHeight - margin - cardHeight
    }
    return RectF(left, top, left + cardWidth, top + cardHeight)
}

private fun Int.inverseRgb(): Int = (this xor RGB_MASK) or OPAQUE_ALPHA
