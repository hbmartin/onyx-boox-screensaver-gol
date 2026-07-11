package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.OverlayCorner

fun createCompositeBitmap(
    width: Int,
    height: Int,
    backgroundColor: Int,
    layers: List<RenderLayer>,
): Bitmap {
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    renderCompositeBitmap(
        bitmap = bitmap,
        backgroundColor = backgroundColor,
        layers = layers,
    )
    return bitmap
}

fun renderCompositeBitmap(
    bitmap: Bitmap,
    backgroundColor: Int,
    layers: List<RenderLayer>,
) {
    val canvas = Canvas(bitmap)
    canvas.drawColor(backgroundColor)
    val paint = Paint()
    val width = bitmap.width
    val height = bitmap.height

    layers.forEach { layer ->
        val numRows = layer.grid.size
        val numCols = layer.grid.firstOrNull()?.size ?: 0
        if (numRows == 0 || numCols == 0) return@forEach
        paint.color = layer.fgColor

        layer.grid.forEachIndexed { row, cells ->
            cells.forEachIndexed { col, isAlive ->
                if (isAlive) {
                    canvas.drawRect(
                        ((col * width) / numCols).toFloat(),
                        ((row * height) / numRows).toFloat(),
                        (((col + 1) * width) / numCols).toFloat(),
                        (((row + 1) * height) / numRows).toFloat(),
                        paint,
                    )
                }
            }
        }
    }
}

fun renderDeviceBitmap(
    context: Context,
    bitmap: Bitmap,
    backgroundColor: Int,
    layers: List<RenderLayer>,
    overlays: DeviceOverlays,
) {
    renderCompositeBitmap(
        bitmap = bitmap,
        backgroundColor = backgroundColor,
        layers = layers,
    )
    val agenda = overlays.calendarAgenda?.takeIf { overlays.calendarSettings.isEnabled }
    val isCalendarDrawn = agenda != null
    if (agenda != null) {
        drawCalendarOverlay(
            context = context,
            bitmap = bitmap,
            agenda = agenda,
            settings = overlays.calendarSettings,
            backgroundColor = backgroundColor,
        )
    }
    if (overlays.stats != null) {
        drawStatsOverlay(
            bitmap = bitmap,
            text = context.getString(
                R.string.stats_overlay,
                overlays.stats.generation,
                overlays.stats.population,
            ),
            textColor = backgroundColor.inverseRgb(),
            backgroundColor = backgroundColor,
            position = if (
                isCalendarDrawn && overlays.calendarSettings.corner == OverlayCorner.BOTTOM_LEFT
            ) {
                StatsOverlayPosition.BOTTOM_RIGHT
            } else {
                StatsOverlayPosition.BOTTOM_LEFT
            },
        )
    }
}

fun createBitmapFromBooleanArray(
    width: Int,
    height: Int,
    trueColor: Int,
    falseColor: Int,
    grid: Array<BooleanArray>,
): Bitmap {
    val bitmap = createCompositeBitmap(
        width = width,
        height = height,
        backgroundColor = falseColor,
        layers = listOf(RenderLayer(grid = grid, fgColor = trueColor)),
    )
    return bitmap
}
