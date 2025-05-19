package me.haroldmartin.golwallpaper.utils

import android.content.Context
import android.net.Uri
import android.util.Log

private const val TAG = "DeleteImage"

@Suppress("TooGenericExceptionCaught")
fun deleteImage(context: Context, uriToDelete: Uri) {
    val resolver = context.contentResolver
    try {
        val rowsDeleted = resolver.delete(uriToDelete, null, null)

        if (rowsDeleted > 0) {
            Log.d(TAG, "Image deleted successfully")
        } else {
            Log.d(TAG, "Image not found or not deleted")
        }
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException while deleting image: $uriToDelete", e)
    } catch (e: Exception) {
        Log.e(TAG, "Exception while deleting image: $uriToDelete", e)
    }
}
