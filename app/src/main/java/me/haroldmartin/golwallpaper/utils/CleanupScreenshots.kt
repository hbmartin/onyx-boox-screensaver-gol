package me.haroldmartin.golwallpaper.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

private const val TAG = "CleanupScreenshots"

/**
 * Removes every screensaver image the app has left in the Downloads collection except [keep].
 *
 * Each worker tick writes a new `screenshot_*.png` and normally deletes the previous one, but a
 * killed process or a failed delete can orphan files that then accumulate in the user's Downloads
 * folder forever. Sweeping on every save keeps only the image currently in use.
 *
 * @param keep the just-saved image that must survive the sweep, or `null` to remove them all.
 * @return the number of orphaned images deleted.
 */
@Suppress("TooGenericExceptionCaught")
fun deleteOrphanedScreenshots(context: Context, keep: Uri?): Int {
    val resolver = context.contentResolver
    val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    val keepId = keep?.let { ContentUris.parseId(it) }
    var deleted = 0

    try {
        resolver.query(
            collection,
            arrayOf(MediaStore.Downloads._ID),
            "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
            arrayOf("$SCREENSHOT_FILE_PREFIX%"),
            null,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                if (id != keepId) {
                    deleted += resolver.deleteById(collection, id)
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to query for orphaned screenshots", e)
    }

    if (deleted > 0) {
        Log.d(TAG, "Removed $deleted orphaned screenshot(s)")
    }
    return deleted
}

private fun ContentResolver.deleteById(collection: Uri, id: Long): Int {
    val uri = ContentUris.withAppendedId(collection, id)
    return runCatching { delete(uri, null, null) }
        .onFailure { Log.e(TAG, "Failed to delete orphan $uri", it) }
        .getOrDefault(0)
}
