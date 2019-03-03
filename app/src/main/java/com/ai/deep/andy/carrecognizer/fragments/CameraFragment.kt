package com.ai.deep.andy.carrecognizer.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ai.deep.andy.carrecognizer.R
import android.widget.FrameLayout
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.camera.CameraPreview
import com.ai.deep.andy.carrecognizer.utils.Logger
import kotlinx.android.synthetic.main.fragment_classification_list.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CameraFragment : Fragment() {

    private var safeToTakePicture = true
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private val mPicture = Camera.PictureCallback { data, _ ->
        val pictureFile: File = getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) ?: run {
            Log.d(Logger.LOGTAG, ("Error creating media file, check storage permissions"))
            safeToTakePicture = true
            return@PictureCallback
        }

        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()

            listener?.captureImageWithCamera(pictureFile)
        } catch (e: FileNotFoundException) {
            Log.d(Logger.LOGTAG, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(Logger.LOGTAG, "Error accessing file: ${e.message}")
        }
        safeToTakePicture = true
    }


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnCameraFragmentInteraction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_camera, container, false)
        mPreview?.also {
            if(it.parent != null){
                (it.parent as ViewGroup).removeView(it)
            }
            v.findViewById<FrameLayout>(R.id.camera_preview).addView(it)
        }

        val captureButton: FloatingActionButton = v.findViewById(R.id.capture_image)
        captureButton.setOnClickListener {
            if (safeToTakePicture) {
                mCamera?.takePicture(null, null, mPicture)
                safeToTakePicture = false;
            }
            else{
                Toast.makeText(context, "Its not safe to create picture now!", Toast.LENGTH_SHORT).show()
            }
        }

        val galleryButton :FloatingActionButton = v.findViewById(R.id.gallery_button)
        galleryButton.setOnClickListener {
            listener?.selectImageFromGallery()
        }

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCameraFragmentInteraction) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }

        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            // Create our Preview view
            CameraPreview(context, it)
        }
        this.safeToTakePicture = true
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        releaseCamera()
    }


    interface OnCameraFragmentInteraction {
        fun captureImageWithCamera(f:  File)
        fun selectImageFromGallery()
    }

    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    private fun releaseCamera() {
        mCamera?.release() // release the camera for other applications
        mCamera = null
    }

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CameraFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

}
