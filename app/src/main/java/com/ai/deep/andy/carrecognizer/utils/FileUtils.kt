package com.ai.deep.andy.carrecognizer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    @SuppressLint("SimpleDateFormat")
    private fun getFileName(type : Int) : String {
        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Carrecognizer"
        )

        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d(Logger.LOGTAG, "failed to create directory")
                    throw Exception("Failed to create directory")
                }
            }
        }
        var fileName = ""
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        when(type) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                fileName = "${mediaStorageDir.path}${File.separator}CAR_IMG_$timeStamp.jpg"
            }
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                fileName = "${mediaStorageDir.path}${File.separator}CAR_IMG_$timeStamp.jpg"
            }
            else -> throw Exception("Unknown file extension")
        }
        Log.i(Logger.LOGTAG, "New file name: $fileName")
        return fileName
    }

    fun getTempFile(context: Context, type: Int): File? =
            Uri.parse(getFileName(type))?.lastPathSegment?.let { filename ->
                File.createTempFile(filename, null, context.cacheDir)
            }

    fun getOutputMediaFile(type: Int): File {
        return File(getFileName(type))
    }


}