package com.ai.deep.andy.carrecognizer.fragments

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ai.deep.andy.carrecognizer.R
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Color
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import com.ai.deep.andy.carrecognizer.model.ClassificationItem
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.services.core.ClassifyService
import com.ai.deep.andy.carrecognizer.services.core.ListClassificationService
import com.ai.deep.andy.carrecognizer.services.statistics.AvgClassificationTimeService
import com.ai.deep.andy.carrecognizer.utils.ClassifierUtils
import com.ai.deep.andy.carrecognizer.utils.FileUtils
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants.Companion.HIGHCOMPRESSIONSIZE
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants.Companion.MEDIUMCOMPRESSIONSIZE
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants.Companion.SUPERHIGHCOMPRESSIONSIZE
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.ai.deep.andy.carrecognizer.utils.MyAnimationUtils
import kotlinx.android.synthetic.main.fragment_classify.*
import nl.dionsegijn.konfetti.KonfettiView
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ClassifyFragment : Fragment() {

    private var imageBitmap: Bitmap? = null
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnClassifyFragmentListener? = null
    private var frameLayout : FrameLayout? = null
    private var konfettiView : KonfettiView? = null

    private var averageClassificationTime = 0
    private var processStep = 0
    private val processResolution = 10

    enum class ClassificationState{
        NOT_STARTED, IN_PROGRESS, ERROR, DONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun changeImageFile(image: Bitmap){
        imageBitmap = image
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v : View = inflater.inflate(R.layout.fragment_classify, container, false)
        val backButton: FloatingActionButton = v.findViewById(R.id.back_to_camera)
        val classifyButton : FloatingActionButton = v.findViewById(R.id.classify_button)
        val saveButton : FloatingActionButton = v.findViewById(R.id.save_classification_button)
        frameLayout = v.findViewById(R.id.frame_container)
        v.findViewById<ImageView>(R.id.my_image_container).setImageBitmap(imageBitmap)
        classifyButton.isEnabled = false

        konfettiView = v.findViewById(R.id.viewKonfetti)

        AvgClassificationTimeService(context!!, object : VolleyOnEventListener<Int>{
            override fun onSuccess(obj: Int) {
                averageClassificationTime = obj

                Log.i(Logger.LOGTAG, "Average classification time: " + averageClassificationTime)
                processStep = (obj / processResolution)
                classifyButton.isEnabled = true
                backButton.setOnClickListener {
                    listener?.goBackToCamera()
                }

                classifyButton.setOnClickListener{
                    doClassification()
                }

                saveButton.setOnClickListener{
                    saveImage()
                }

                changeLayout(ClassificationState.NOT_STARTED)
            }

            override fun onFailure(e: Exception) {
                val errorMsg : String =  if (e.message == null) "Unexpected error, please try again later" else e.message!!
                Log.e(Logger.LOGTAG, errorMsg)
                setError(errorMsg)
            }
        }).getAvarageUsageStatistics()

        return v
    }

    fun changeLayout(state: ClassificationState){
        classification_result_layout?.visibility = if (state == ClassificationState.DONE) View.VISIBLE else View.GONE
        classification_in_progress_layout?.visibility = if (state == ClassificationState.IN_PROGRESS) View.VISIBLE else View.GONE
        classification_not_started_layout?.visibility =if (state == ClassificationState.NOT_STARTED) View.VISIBLE else View.GONE
        classification_error_layout?.visibility = if (state == ClassificationState.ERROR) View.VISIBLE else View.GONE
    }

    fun setError(message : String){
        Log.e(Logger.LOGTAG, "Wow unexpected error: " + message)
        changeLayout(ClassificationState.ERROR)
        classification_error_text.text = message
    }

    fun doClassification(){
        Log.i(Logger.LOGTAG, "Start processing classification request..")
        changeLayout(ClassificationState.IN_PROGRESS)
        number_progress_bar.progress = 0

        compressImage()

        val t = Thread(Runnable {
            for (i in 1..processResolution) {
                try {
                    val p = ((i * processStep).div(averageClassificationTime.toDouble()) * 100).toInt()
                    Log.i(Logger.LOGTAG, p.toString())

                    activity?.runOnUiThread(java.lang.Runnable {
                        number_progress_bar.progress = (if (p <= 100) p else 100)
                    })

                    Thread.sleep(processStep.toLong())
                } catch (e: InterruptedException){
                    Log.d(Logger.LOGTAG, "Thread interrupted, but it's ok, classification ended")
                }
            }
        })
        t.start()

        ClassifyService(context!!, object : VolleyOnEventListener<JSONObject> {
            override fun onSuccess(obj: JSONObject) {
                if(t.isAlive){
                    t.interrupt()
                }
                number_progress_bar.progress = 100
                Log.i(Logger.LOGTAG, "Classification was successful")

                changeLayout(ClassificationState.DONE)
                classify_button?.visibility = View.GONE

                classification_results?.text = ClassificationItem(obj).getFormattedResults()
                val settings = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
                if(settings.getBoolean("extra_anim", true)){
                    MyAnimationUtils.showKonfetti(konfettiView)
                }
            }

            override fun onFailure(e: Exception) {
                if(t.isAlive){
                    t.interrupt()
                }
                val errorMsg : String =  if (e.message == null) "Unexpected classification error, please try again later" else e.message!!
                number_progress_bar.progress = 0
                Log.e(Logger.LOGTAG, errorMsg)
                setError(errorMsg)
            }
        }).classifyImage(imageBitmap!!)
    }

    private fun compressImage(){
        val settings = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
        val comp = settings.getString("compress_rate", null)
        when (comp) {
            "1" -> {
                Log.i(Logger.LOGTAG, "Picture not compressed")
            }
            "2" -> {
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, MEDIUMCOMPRESSIONSIZE, MEDIUMCOMPRESSIONSIZE, false)
                Log.i(Logger.LOGTAG, "Picture compress into $MEDIUMCOMPRESSIONSIZE")
            }
            "3" -> {
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, HIGHCOMPRESSIONSIZE, HIGHCOMPRESSIONSIZE, false)
                Log.i(Logger.LOGTAG, "Picture compress into $HIGHCOMPRESSIONSIZE")

            }
            "4" -> {
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, SUPERHIGHCOMPRESSIONSIZE, SUPERHIGHCOMPRESSIONSIZE, false)
                Log.i(Logger.LOGTAG, "Picture compress into $SUPERHIGHCOMPRESSIONSIZE")

            }
            else -> {
                Log.i(Logger.LOGTAG, "Picture not compressed")
            }
        }
    }

    fun saveImage(){
        Log.i(Logger.LOGTAG, "Saving image started")
        val file : File = FileUtils.getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            // Use the compress method on the BitMap object to write image to the OutputStream
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)

            if(frameLayout != null) {
                Snackbar.make(frameLayout!!, "Image saved", Snackbar.LENGTH_SHORT).show()
            }
            Log.i(Logger.LOGTAG, "Image saved as " + file.name)
        } catch (e: Exception) {
            Log.e(Logger.LOGTAG, "Failed to save image", e)
            if(frameLayout != null) {
                Snackbar.make(frameLayout!!, "Failed to save image, try again", Snackbar.LENGTH_SHORT).show()
            }
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClassifyFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnClassifyFragmentListener {
        fun goBackToCamera()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ClassifyFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
