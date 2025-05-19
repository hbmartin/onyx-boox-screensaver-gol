package me.haroldmartin.golwallpaper.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

private const val TAG = "SavImage"
private const val QUALITY = 100

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
    val dir = context.getFilesDir()
    val file = File(dir, fileName)
    Log.d(TAG, "file: $file") // Log

    try {
        FileOutputStream(file).use { it.writeBitmap(bitmap) }
    } catch (e: IOException) {
        Log.e(TAG, "Failed to save bitmap to file", e)
        return null
    }

    return file.absolutePath
}

internal fun saveBitmapToPictures(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
): Pair<Uri, String>? {
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
    }

    return uri?.let { it to "/storage/emulated/0/Download/$fileName" }
}

private fun OutputStream.writeBitmap(bitmap: Bitmap) {
    bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, this)
    this.flush()
}
