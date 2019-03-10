package com.ai.deep.andy.carrecognizer.services.core

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.ai.deep.andy.carrecognizer.model.User
import com.ai.deep.andy.carrecognizer.services.VolleyMultipartRequest
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants
import com.ai.deep.andy.carrecognizer.utils.ImageUtils
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.orm.SugarRecord
import org.json.JSONObject
import com.android.volley.toolbox.JsonObjectRequest
import java.io.ByteArrayOutputStream


class ClassifyService(context: Context, callback: VolleyOnEventListener<JSONObject>) {

    var context: Context? = context
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<JSONObject>? = callback
    //val BASE_URL = "http://carrecognizer.northeurope.cloudapp.azure.com/core/"
    val BASE_URL = "http://192.168.0.185/core/classify/"
    var currentUser: User? = null

    init {
        this.queue = Volley.newRequestQueue(context)
    }

    fun getUser(): User? {
        //TODO cache the authenticated user!
        val users = SugarRecord.findAll(User::class.java)
        if (users.hasNext()) {
            return users.next()
        }
        return null
    }


    fun classifyImage(bitmap: Bitmap) {
        currentUser = getUser()
        var startTime = System.currentTimeMillis()
        Log.i(Logger.LOGTAG, "Classification POST started at " + startTime.toString())

        val request = object : VolleyMultipartRequest(Request.Method.POST, BASE_URL,
                Response.Listener<JSONObject> { response ->
                    Log.i(Logger.LOGTAG, "Classification took " + (System.currentTimeMillis() - startTime / 1000).toString() + " seconds")
                    Log.d(Logger.LOGTAG, response.toString())
                    mCallBack?.onSuccess(response)
                }, Response.ErrorListener { error ->
            Log.e(Logger.LOGTAG, error.networkResponse.statusCode.toString())
            mCallBack?.onFailure(error)
        }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return java.util.HashMap()
            }

            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = GlobalConstants.JWT_PREFIX + " " + currentUser?.jwtToken
                return headers
            }

            override fun getByteData(): Map<String, DataPart> {
                val params = java.util.HashMap<String, DataPart>()
                val imagename = startTime
                params["carpic"] = DataPart(imagename.toString() + ".jpg", ImageUtils.getFileDataFromDrawable(bitmap))
                return params
            }
        }

        queue!!.add(request)
    }
}