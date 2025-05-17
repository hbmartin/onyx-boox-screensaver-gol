package me.haroldmartin.golwallpaper.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

private const val TAG = "BitmapRenderer"
private const val QUALITY = 100

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

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
    val dir = context.getFilesDir()
    val file = File(dir, fileName)
    Log.d(TAG, "file: $file") // Log

    try {
        FileOutputStream(file).use { it.writeBitmap(bitmap) }
    } catch (e: IOException) {
        Log.e(TAG, "Failed to save bitmap to file", e)
        return null
    } finally {
        bitmap.recycle()
    }

    return file.absolutePath
}

internal fun saveBitmapToPictures(context: Context, bitmap: Bitmap, fileName: String): String? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "image/png")
    }
    val resolver = context.contentResolver
    val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    try {
        uri?.let {
            resolver.openOutputStream(it)?.use { out -> out.writeBitmap(bitmap) }
        }
    } catch (e: IOException) {
        Log.e(TAG, "Failed to save bitmap to file", e)
        return null
    } finally {
        bitmap.recycle()
    }

    return uri?.let { "/storage/emulated/0/Download/$fileName" }
}

private fun OutputStream.writeBitmap(bitmap: Bitmap) {
    bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, this)
    this.flush()
}
