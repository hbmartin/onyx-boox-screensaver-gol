package me.haroldmartin.golwallpaper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap

fun createBitmapFromBooleanArray(
    width: Int,
    height: Int,
    trueColor: Int,
    falseColor: Int,
    grid: Array<BooleanArray>,
): Bitmap {
    val numRows = grid.size
    val numCols = if (numRows > 0) grid[0].size else 0

    require(numCols > 0) { "Columns must be greater than 0" }
    require(numRows > 0) { "Rows must be greater than 0" }

    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Iterate through each cell in the grid
    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            // Set the color based on the boolean value
            paint.color = if (grid[row][col]) trueColor else falseColor

            // Calculate the bounds of the cell
            val left = (col * width) / numCols
            val top = (row * height) / numRows
            val right = ((col + 1) * width) / numCols
            val bottom = ((row + 1) * height) / numRows

            // Draw the cell as a filled rectangle
            canvas.drawRect(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                paint,
            )
        }
    }

    return bitmap
}
