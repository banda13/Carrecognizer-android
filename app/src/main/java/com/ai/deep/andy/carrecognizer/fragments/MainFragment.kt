package com.ai.deep.andy.carrecognizer.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ai.deep.andy.carrecognizer.R
import java.io.File

private const val CAMERA_FRAGMENT_TAG = "camera_tag"
private const val CLASSIFY_FRAGMENT_TAG = "classify_tag"

class MainFragment : Fragment() {

    private var cameraTag: String? = null
    private var classifyTag: String? = null

    private var cameraFragment: CameraFragment = CameraFragment.newInstance("", "")
    private var classifyFragment: ClassifyFragment = ClassifyFragment.newInstance("", "");

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cameraTag = it.getString(CAMERA_FRAGMENT_TAG)
            classifyTag = it.getString(CLASSIFY_FRAGMENT_TAG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mainfragment, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        changeToCameraFragment()
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MainFragment().apply {
                    arguments = Bundle().apply {
                        putString(CAMERA_FRAGMENT_TAG, param1)
                        putString(CLASSIFY_FRAGMENT_TAG, param2)
                    }
                }
    }

    fun changeToCameraFragment(){
        activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, cameraFragment, cameraTag)
                ?.addToBackStack(null)
                ?.commit()
    }

    fun changeToClassifyFragment(f: File){
        activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, classifyFragment, classifyTag)
                ?.addToBackStack(null)
                ?.commit()
        classifyFragment.changeImageFile(f)
    }
}
