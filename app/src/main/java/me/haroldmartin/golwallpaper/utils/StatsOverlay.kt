package me.haroldmartin.golwallpaper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface

private const val TEXT_SIZE_DIVISOR = 40f
private const val TEXT_PADDING_DIVISOR = 2f

// Draws the given text in the bottom-left corner of the bitmap on top of an opaque
// background box so it stays readable over live cells.
fun drawStatsOverlay(bitmap: Bitmap, text: String, textColor: Int, backgroundColor: Int) {
    val canvas = Canvas(bitmap)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = bitmap.height / TEXT_SIZE_DIVISOR
        typeface = Typeface.MONOSPACE
    }
    val padding = textPaint.textSize / TEXT_PADDING_DIVISOR
    val baseline = bitmap.height - padding - textPaint.fontMetrics.descent
    val backgroundPaint = Paint().apply { color = backgroundColor }

    canvas.drawRect(
        0f,
        baseline + textPaint.fontMetrics.ascent - padding,
        padding + textPaint.measureText(text) + padding,
        bitmap.height.toFloat(),
        backgroundPaint,
    )
    canvas.drawText(text, padding, baseline, textPaint)
}
