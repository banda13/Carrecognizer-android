package com.ai.deep.andy.carrecognizer.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import com.ai.deep.andy.carrecognizer.R
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.services.core.CategoryDetailsService
import com.ai.deep.andy.carrecognizer.utils.Logger
import org.json.JSONObject
import android.R.attr.button
import android.widget.RelativeLayout
import android.annotation.SuppressLint
import android.os.Build




class DialogFactory {

    private fun addKeyAndValueTextToLayout(key: String, value: String, layout : LinearLayout, context: Context){
        Log.i(Logger.LOGTAG, "Creating layout for $key - $value")
        val subContainer = RelativeLayout(context)
        layout.addView(subContainer)

        val keyView = TextView(context)
        keyView.setTypeface(null, Typeface.BOLD)
        keyView.text = key

        val startParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        startParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            startParams.addRule(RelativeLayout.ALIGN_PARENT_START)
        }

        keyView.layoutParams = startParams
        subContainer.addView(keyView)

        val valueView = TextView(context)
        valueView.text = value

        val endParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        endParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            endParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        }

        valueView.layoutParams = endParams
        subContainer.addView(valueView)
    }

    fun showLoadingDialog(title: String?, context : Context): Dialog{
        val dialogs = Dialog(context)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setCancelable(true)
        dialogs.setContentView(R.layout.dialog_loading)
        val titleView = dialogs.findViewById(R.id.loading_title) as TextView
        titleView.text = title

        dialogs.show()
        return dialogs
    }

    fun showItemDetails(titles: List<String?>, context : Context, params: JSONObject): Dialog{
        val dialogs = Dialog(context)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setCancelable(true)
        dialogs.setContentView(R.layout.dialog_item_details)
        val titleSpinner = dialogs.findViewById(R.id.title) as Spinner
        var check = 0
        val arrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, titles)

        titleSpinner.adapter = arrayAdapter

        titleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //ok?
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(++check > 1) { // cause adding listener auto trigger onItemSelected and its looping lol wtf ???!
                    if (titles[position] == titleSpinner.selectedItem) {
                        return
                    }
                    val dialog = DialogFactory().showLoadingDialog(titles[position], context)
                    CategoryDetailsService(context, object : VolleyOnEventListener<JSONObject> {
                        override fun onSuccess(obj: JSONObject) {
                            dialog.dismiss()
                            DialogFactory().showItemDetails(titles, context, obj)
                        }

                        override fun onFailure(e: Exception) {
                            dialog.dismiss()
                            Toast.makeText(context, "Sry, can't load the details for this classification, try later", Toast.LENGTH_LONG).show()
                        }
                    }).getDetails(titles[position])
                }
            }
        }

        val mainContainerView = dialogs.findViewById(R.id.details_container) as LinearLayout

        for (key in params.keys()) {
            addKeyAndValueTextToLayout(key, params.getString(key), mainContainerView, context)
        }

        val closeButton = dialogs.findViewById(R.id.close) as Button
        closeButton.setOnClickListener { dialogs.dismiss() }
        dialogs.show()
        return dialogs
    }
}