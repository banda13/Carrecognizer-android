package com.ai.deep.andy.carrecognizer.fragments

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
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_classify.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ClassifyFragment : Fragment() {

    private var imageBitmap: Bitmap? = null
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnClassifyFragmentListener? = null

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
        backButton.setOnClickListener {
            listener?.goBackToCamera()
        }
        v.findViewById<ImageView>(R.id.my_image_container).setImageBitmap(imageBitmap)
        return v
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
