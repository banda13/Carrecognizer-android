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


private const val CAMERA_FRAGMENT_TAG = "CameraFragment"
private const val CLASSIFY_FRAGMENT_TAG = "ClassifyFragment"

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
            (fragment as MainFragment).changeToClassifyFragment(imgBitmap)
        }
    }

    fun selectImageFromGallery(f: Bitmap){
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if(container?.currentItem == 1 && fragment != null){
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
                selectImageFromGallery(bitmap)
            } catch (e: IOException) {
                Log.e(Logger.LOGTAG, e.message)
            }

        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when(position){
                0 -> {
                    return ClassificationListFragment.newInstance(1)
                }
                1 -> {
                    return MainFragment.newInstance(CAMERA_FRAGMENT_TAG, CLASSIFY_FRAGMENT_TAG);
                }
                2 -> {
                    return UserFragment.newInstance("User", "Fragment");
                }
                else -> {
                    return MainFragment.newInstance("Camera", "Fragment");
                }
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }
}
