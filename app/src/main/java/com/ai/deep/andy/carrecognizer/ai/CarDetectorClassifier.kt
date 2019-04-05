package com.ai.deep.andy.carrecognizer.ai

import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.ai.deep.andy.carrecognizer.utils.Logger
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.ai.deep.andy.carrecognizer.ai.IClassifer.Recognition
import android.annotation.SuppressLint
import java.lang.Float
import java.util.*
import kotlin.experimental.and
import java.nio.file.Files.size




object CarDetectorClassifier: IClassifer{

    private val MAX_RESULTS = 3
    private val BATCH_SIZE = 1
    private val PIXEL_SIZE = 3
    private val THRESHOLD = 0.1f

    private val IMAGE_MEAN = 128
    private val IMAGE_STD = 128.0f

    private var interpreter: Interpreter? = null
    private var inputSize: Int = 0
    private var labelList: List<String>? = null
    private var quant: Boolean = false


    fun create(assetManager: AssetManager, modelPath: String, labelPath : String, inputSize: Int, quant: Boolean): CarDetectorClassifier{
        Log.i(Logger.LOGTAG, "Initializing car detector classifier")
        CarDetectorClassifier.interpreter = Interpreter(CarDetectorClassifier.loadModelFile(assetManager, modelPath))
        CarDetectorClassifier.labelList = CarDetectorClassifier.loadLabelList(assetManager, labelPath)
        CarDetectorClassifier.inputSize = inputSize
        CarDetectorClassifier.quant = quant

        return CarDetectorClassifier
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        Log.i(Logger.LOGTAG, "Model loaded from " + modelPath)
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        val labelList = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelPath)))
        var line = reader.readLine()
        while(line != null){
            labelList.add(line)
            line = reader.readLine()
        }
        reader.close()

        Log.i(Logger.LOGTAG, "Model labels loaded: " + labelList.toString())
        return labelList
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        Log.i(Logger.LOGTAG, "Converting bitmap to bytebuffer..")
        val byteBuffer: ByteBuffer = if (quant) {
            ByteBuffer.allocateDirect(1 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE)
        } else {
            ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE)
        }

        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                if (quant) {
                    byteBuffer.put(((value shr 16) and 0xFF).toByte())
                    byteBuffer.put(((value shr 8) and 0xFF).toByte())
                    byteBuffer.put((value and 0xFF).toByte())
                } else {
                    byteBuffer.putFloat((((value shr 16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    byteBuffer.putFloat((((value shr 8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    byteBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }

            }
        }
        return byteBuffer
    }

    @SuppressLint("DefaultLocale")
    private fun getSortedResultByte(labelProbArray: Array<ByteArray>): List<Recognition> {

        val pq = PriorityQueue(
                MAX_RESULTS,
                Comparator<Recognition> { lhs, rhs -> Float.compare(rhs.confidence!!, lhs.confidence!!) })

        for (i in 0 until labelList!!.size) {
            val confidence = (labelProbArray[0][i] and 0xff.toByte()) / 255.0f
            if (confidence > THRESHOLD) {
                pq.add(Recognition("" + i,
                        if (labelList!!.size > i) labelList!!.get(i) else "unknown", confidence, quant))
            }
        }

        val recognitions = ArrayList<Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }

        return recognitions
    }

    private fun getSortedResultFloat(labelProbArray: Array<FloatArray>): List<Recognition> {

        val pq = PriorityQueue(
                MAX_RESULTS,
                object : Comparator<Recognition> {
                    override fun compare(lhs: Recognition, rhs: Recognition): Int {
                        return java.lang.Float.compare(rhs.confidence!!, lhs.confidence!!)
                    }
                })

        for (i in 0 until labelList!!.size) {
            val confidence = labelProbArray[0][i]
            if (confidence > THRESHOLD) {
                pq.add(Recognition("" + i,
                        if (labelList!!.size > i) labelList!!.get(i) else "unknown",
                        confidence, quant))
            }
        }

        val recognitions = ArrayList<Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }

        return recognitions
    }


    override fun recognizeImage(bitmap: Bitmap): List<IClassifer.Recognition> {
        Log.i(Logger.LOGTAG, "Recognizing image bitmap started")
        val startTime = System.currentTimeMillis()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        return if (quant) {
            val result = Array(1) { ByteArray(labelList!!.size) }
            interpreter?.run(byteBuffer, result)
            Log.i(Logger.LOGTAG, "Classification took " + (System.currentTimeMillis() - startTime) + " MS")
            getSortedResultByte(result)
        } else {
            val result = Array(1) { FloatArray(labelList!!.size) }
            interpreter?.run(byteBuffer, result)
            Log.i(Logger.LOGTAG, "Classification took " + (System.currentTimeMillis() - startTime) + " MS")
            getSortedResultFloat(result)
        }
    }

    override fun close() {
        if(interpreter != null){
            interpreter!!.close()
            interpreter = null
        }
    }
}