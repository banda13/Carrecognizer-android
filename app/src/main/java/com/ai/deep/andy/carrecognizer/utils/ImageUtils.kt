package com.ai.deep.andy.carrecognizer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun convertYuvToJpeg(yuvData: ByteArray, camera: Camera): ByteArray{
        val cameraParameters = camera.parameters
        val width = cameraParameters.previewSize.width
        val height = cameraParameters.previewSize.height
        val yuv = YuvImage(yuvData, cameraParameters.previewFormat, width, height, null)
        val ms = ByteArrayOutputStream()
        val quality = 80 // adjust this as needed
        yuv.compressToJpeg(Rect(0, 0, width, height), quality, ms)
        return ms.toByteArray()
    }

    fun bytesToBitmap(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


}