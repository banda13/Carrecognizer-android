package com.ai.deep.andy.carrecognizer.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}