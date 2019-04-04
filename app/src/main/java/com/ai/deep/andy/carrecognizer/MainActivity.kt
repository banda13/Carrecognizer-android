package com.ai.deep.andy.carrecognizer

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.fragments.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.io.File
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.ai.deep.andy.carrecognizer.utils.Logger
import java.io.IOException
import android.os.AsyncTask.execute
import com.ai.deep.andy.carrecognizer.ai.CarDetectorClassifier
import com.ai.deep.andy.carrecognizer.ai.IClassifer
import java.util.concurrent.Executors


private const val CAMERA_FRAGMENT_TAG = "CameraFragment"
private const val CLASSIFY_FRAGMENT_TAG = "ClassifyFragment"

private var classifier: CarDetectorClassifier? = null
private val executor = Executors.newSingleThreadExecutor()

private const val MODEL_PATH = "converted_model.tflite"
private const val QUANT = true
private const val LABEL_PATH = "labels.txt"
private const val INPUT_SIZE = 150

private const val PICK_IMAGE_REQUEST = 1

class MainActivity : AppCompatActivity(), CameraFragment.OnCameraFragmentInteraction, ClassifyFragment.OnClassifyFragmentListener {

    override fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun goBackToCamera() {
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if(container?.currentItem == 1 && fragment != null){
            (fragment as MainFragment).changeToCameraFragment()
        }
    }

    override fun captureImageWithCamera(f: File) {
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if(container?.currentItem == 1 && fragment != null){
            val imgBitmap = BitmapFactory.decodeFile(f.absolutePath)
            Log.i(Logger.LOGTAG, "Image bitmap created, detecting if its a car or not")
            val results : List<IClassifer.Recognition> = classifier!!.recognizeImage(imgBitmap)
            Log.i(Logger.LOGTAG, results.toString())
            (fragment as MainFragment).changeToClassifyFragment(imgBitmap)
        }
    }

    fun selectImageFromGallery(f: Bitmap){
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if(container?.currentItem == 1 && fragment != null){
            Log.i(Logger.LOGTAG, "Image bitmap created, detecting if its a car or not")
            val results : List<IClassifer.Recognition> = classifier!!.recognizeImage(f)
            Log.i(Logger.LOGTAG, results.toString())
            (fragment as MainFragment).changeToClassifyFragment(f)
        }
    }


    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        container.currentItem = 1

        initTensorFlowAndLoadModel()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            val uri = data.data

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                Log.i(Logger.LOGTAG, "Image successfully selected from gallery, bitmap created")
                selectImageFromGallery(bitmap)
            } catch (e: IOException) {
                Log.e(Logger.LOGTAG, e.message)
            }

        }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute({
            try {
                Log.i(Logger.LOGTAG, "Initializing car detector classifier")
                classifier = CarDetectorClassifier.create(
                        assets,
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE,
                        QUANT)
                Log.i(Logger.LOGTAG, "Classifier initialized")
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(Logger.LOGTAG, "Context destroyed, closing classifier")
        executor.execute({ classifier?.close() })
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> {
                    Log.i(Logger.LOGTAG, "Switching to classification fragment")
                    ClassificationListFragment.newInstance(1)
                }
                1 -> {
                    Log.i(Logger.LOGTAG, "Switching to main fragment")
                    MainFragment.newInstance(CAMERA_FRAGMENT_TAG, CLASSIFY_FRAGMENT_TAG)
                }
                2 -> {
                    Log.i(Logger.LOGTAG, "Switching to user fragment")
                    UserFragment.newInstance("User", "Fragment")
                }
                else -> {
                    Log.w(Logger.LOGTAG, "Switching to main fragment, in default case..")
                    MainFragment.newInstance("Camera", "Fragment");
                }
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }
}
