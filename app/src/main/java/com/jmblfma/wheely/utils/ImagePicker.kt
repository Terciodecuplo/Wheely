package com.jmblfma.wheely.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImagePicker {
    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun createImageFile(context: Context): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )

        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    fun fixImageOrientation(context: Context, imgUri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(imgUri)
        inputStream?.let { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close() // Close the first stream

            // Re-open the stream for reading EXIF data
            context.contentResolver.openInputStream(imgUri)?.use { exifStream ->
                val ei = ExifInterface(exifStream)
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }

                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
        return null
    }

}