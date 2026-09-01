package com.notzyvex.fastprint.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

private const val TAG = "ImageStore"

/**
 * Keeps a private copy of every picked image.
 *
 * A picked content:// Uri is only readable for as long as the grant lasts, so history could not
 * reprint from it days later. Copying into app storage is what makes "reprint" actually work.
 */
class ImageStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "images").apply { mkdirs() }

    /** Longest edge kept for a stored image — plenty for 600dpi on a 24" poster, but bounded. */
    private val maxEdge = 4096

    suspend fun importFrom(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeScaled(uri) ?: return@runCatching null
            val target = File(dir, "img_${UUID.randomUUID()}.jpg")
            FileOutputStream(target).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            bitmap.recycle()
            target.absolutePath
        }.onFailure { Log.e(TAG, "import failed for $uri", it) }.getOrNull()
    }

    suspend fun load(path: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (path.isNullOrBlank()) return@withContext null
        runCatching { BitmapFactory.decodeFile(path) }
            .onFailure { Log.w(TAG, "load failed for $path", it) }
            .getOrNull()
    }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    /** Removes stored images no history row references any more. */
    suspend fun pruneOrphans(keep: Set<String>) = withContext(Dispatchers.IO) {
        runCatching {
            dir.listFiles()?.forEach { f ->
                if (f.absolutePath !in keep) f.delete()
            }
        }
    }

    private fun decodeScaled(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > maxEdge) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        return applyExifRotation(uri, decoded)
    }

    /** Phone cameras record orientation in EXIF; without this, portrait shots print sideways. */
    private fun applyExifRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val m = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }
}
