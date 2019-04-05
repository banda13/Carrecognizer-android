package com.ai.deep.andy.carrecognizer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.ai.deep.andy.carrecognizer.utils.Logger
import java.io.IOException
import android.os.AsyncTask.execute
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.ai.deep.andy.carrecognizer.ai.CarDetectorClassifier
import com.ai.deep.andy.carrecognizer.ai.IClassifer
import java.util.concurrent.Executors


private const val CAMERA_FRAGMENT_TAG = "CameraFragment"
private const val CLASSIFY_FRAGMENT_TAG = "ClassifyFragment"

private var classifier: CarDetectorClassifier? = null
private val executor = Executors.newSingleThreadExecutor()

private const val MODEL_PATH = "converted_model_2.tflite"
private const val QUANT = false
private const val LABEL_PATH = "labels.txt"
private const val INPUT_SIZE = 150

private const val PICK_IMAGE_REQUEST = 1

private const val CAMERA_REQUEST_CODE = 101
private const val WRITE_STORAGE_REQUEST_CODE = 102

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
            if(!results[0].title?.equals("car")!! || results[0].confidence!! < 90.0f){
                showAreYouSureDialog(this, fragment, imgBitmap)
            }
            else {
                (fragment as MainFragment).changeToClassifyFragment(imgBitmap)
            }
        }
    }

    private fun selectImageFromGallery(f: Bitmap){
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if(container?.currentItem == 1 && fragment != null){
            Log.i(Logger.LOGTAG, "Image bitmap created, detecting if its a car or not")
            val results : List<IClassifer.Recognition> = classifier!!.recognizeImage(f)
            if(!results[0].title?.equals("car")!! || results[0].confidence!! < 90.0f){
                showAreYouSureDialog(this, fragment, f)
            }
            else {
                (fragment as MainFragment).changeToClassifyFragment(f)
            }
        }
    }

    fun showAreYouSureDialog(context: Context, fragment: Fragment, imgBitmap: Bitmap){
        AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("It's not a car!")
                .setMessage("It doesn't seem like a car, will you continue?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("Create new photo") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(context, "Try different angle, or check help to get more accurate predictions", Toast.LENGTH_SHORT).show()
                    (fragment as MainFragment).changeToClassifyFragment(imgBitmap)
                    goBackToCamera()
                }.setPositiveButton("Continue") { _, _ ->
                    (fragment as MainFragment).changeToClassifyFragment(imgBitmap)
                }.create().show()
    }


    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter

        requestPermissionIfNeeded()

        initTensorFlowAndLoadModel()
    }

    private fun requestPermissionIfNeeded(){
        val cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(Logger.LOGTAG, "Permission to use camera not granted yet, requesting it from user")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
        }
        else if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(Logger.LOGTAG, "Permission to write storage not granted yet, requesting it from user")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_STORAGE_REQUEST_CODE)
        }
        else{
            container.currentItem = 1
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(Logger.LOGTAG, "Camera Permission has been denied by user")
                    Toast.makeText(this, "Using camera is necessary to take pictures of objects", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_REQUEST_CODE)
                } else {
                    Log.i(Logger.LOGTAG, "Camera Permission has been granted by user")
                    val storagePermission = ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                        Log.i(Logger.LOGTAG, "Permission to write storage not granted yet, requesting it from user")
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                WRITE_STORAGE_REQUEST_CODE)
                    }
                }
            }
            WRITE_STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(Logger.LOGTAG, "Write storage permission has been denied by user")
                    Toast.makeText(this, "Writing storage is necessary to save captured images to your gallery, if you click to the save button", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            WRITE_STORAGE_REQUEST_CODE)
                } else {
                    Log.i(Logger.LOGTAG, "Writing storage Permission has been granted by user")
                    val cameraPermission = ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                    if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                        Log.i(Logger.LOGTAG, "Permission to use camera not granted yet, requesting it from user")
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_REQUEST_CODE)
                    }
                    else{
                        container.currentItem = 1
                    }
                }
            }
            else -> {
                Log.e(Logger.LOGTAG, "Unexpected permission request code captured")
            }
        }
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
        executor.execute {
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
        }
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
