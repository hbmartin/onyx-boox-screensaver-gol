package me.haroldmartin.golwallpaper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap

fun createCompositeBitmap(
    width: Int,
    height: Int,
    backgroundColor: Int,
    layers: List<RenderLayer>,
): Bitmap {
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(backgroundColor)
    val paint = Paint()

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

    return bitmap
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
