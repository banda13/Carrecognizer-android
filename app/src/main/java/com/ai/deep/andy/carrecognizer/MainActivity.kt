package com.ai.deep.andy.carrecognizer

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

private const val CAMERA_FRAGMENT_TAG = "CameraFragment"
private const val CLASSIFY_FRAGMENT_TAG = "ClassifyFragment"

class MainActivity : AppCompatActivity(), CameraFragment.OnCameraFragmentInteraction, ClassifyFragment.OnClassifyFragmentListener {

    override fun goBackToCamera() {
        val fragment: Fragment? = supportFragmentManager?.findFragmentByTag("android:switcher:" + R.id.container + ":" + container.currentItem)
        if(container?.currentItem == 1 && fragment != null){
            (fragment as MainFragment).changeToCameraFragment()
        }
    }

    override fun pictureTaken(f: File) {
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
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        container.currentItem = 1


        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
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
