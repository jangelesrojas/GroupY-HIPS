package com.example.hips

// This helper handles copying JPEGs, creating output files, saving to gallery, and preserving EXIF data.


import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// File helper object used by the embed and extract flows.
object StegoFileHelper {

    // Copies a selected image URI into app cache so native code can read it by file path.
    fun copyUriToCacheJpeg(context: Context, uri: Uri, fileName: String): File {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open selected image")

        val outFile = File(context.cacheDir, fileName)

        input.use { inp ->
            FileOutputStream(outFile).use { out ->
                inp.copyTo(out)
            }
        }

        return outFile
    }

    // Creates a temporary output file in cache for the embedded JPEG.
    fun createOutputJpegFile(context: Context): File {
        return File(context.cacheDir, "hips_embed_${System.currentTimeMillis()}.jpg")
    }

    // Saves a JPEG to the phone gallery using MediaStore.
    fun saveJpegToGallery(context: Context, sourceFile: File): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "HIPS_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/HIPS")
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Failed to create MediaStore record")

        resolver.openOutputStream(uri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: throw IOException("Failed to open MediaStore output stream")

        return uri
    }

    // Attempts to copy important EXIF metadata so the output image keeps normal photo details.
    fun tryCopyExif(fromFile: File, toFile: File) {
        try {
            val src = ExifInterface(fromFile.absolutePath)
            val dst = ExifInterface(toFile.absolutePath)

            // These metadata tags are worth preserving from the original image.
            val tags = listOf(
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_WHITE_BALANCE
            )

            for (tag in tags) {
                val value = src.getAttribute(tag)
                if (value != null) {
                    dst.setAttribute(tag, value)
                }
            }

            dst.saveAttributes()
        } catch (_: Exception) {
        }
    }
}
