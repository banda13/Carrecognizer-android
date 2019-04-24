package com.ai.deep.andy.carrecognizer.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ai.deep.andy.carrecognizer.R
import com.ai.deep.andy.carrecognizer.utils.Logger
import org.json.JSONObject
import org.w3c.dom.Text

class DialogFactory {

    private fun addKeyAndValueTextToLayout(key: String, value: String, layout : LinearLayout, context: Context){
        Log.i(Logger.LOGTAG, "Creating layout for $key - $value")
        val subContainer = LinearLayout(context)
        subContainer.orientation = LinearLayout.HORIZONTAL
        layout.addView(subContainer)

        val keyView = TextView(context)
        keyView.setTypeface(null, Typeface.BOLD)
        keyView.text = key
        keyView.gravity = Gravity.START
        subContainer.addView(keyView)

        val valueView = TextView(context)
        valueView.text = value
        valueView.gravity = Gravity.END
        subContainer.addView(valueView)
    }

    fun showItemDetails(title: String, context : Context, params: JSONObject){
        var dialogs = Dialog(context)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setCancelable(false)
        dialogs.setContentView(R.layout.dialog_item_details)
        val titleView = dialogs.findViewById(R.id.title) as TextView
        titleView.text = title

        val mainContainerView = dialogs.findViewById(R.id.details_container) as LinearLayout

        for (key in params.keys()) {
            addKeyAndValueTextToLayout(key, params.getString(key), mainContainerView, context)
        }

        val closeButton = dialogs.findViewById(R.id.close) as Button
        closeButton.setOnClickListener { dialogs.dismiss() }
        dialogs.show()
    }
}