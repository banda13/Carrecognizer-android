package com.ai.deep.andy.carrecognizer.services.core

import android.content.Context
import com.ai.deep.andy.carrecognizer.model.ClassificationItem
import com.ai.deep.andy.carrecognizer.model.User
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.android.volley.toolbox.Volley
import com.orm.SugarRecord
import org.json.JSONObject
import android.util.Log
import com.ai.deep.andy.carrecognizer.model.ClassificationItem.Companion.getItemListFromJson
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest


class ListClassificationService {

    var context : Context? = null
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<List<ClassificationItem>>? = null
    val BASE_URL = "http://carrecognizer.northeurope.cloudapp.azure.com/core/"
    var currentUser : User? = null

    constructor(context: Context, callback: VolleyOnEventListener<List<ClassificationItem>>){
        this.context = context
        this.queue = Volley.newRequestQueue(context)
        this.mCallBack = callback
    }

    fun getUser() : User? {
        //TODO cache the authenticated user!
        val users = SugarRecord.findAll(User::class.java)
        if(users.hasNext()){
            return users.next()
        }
        return null
    }

    fun getItems(page: Int){
        currentUser = getUser()

        val url = BASE_URL + "classlist?page=" + page

        val req = object : JsonObjectRequest(Request.Method.GET, url, null, object : Response.Listener<JSONObject> {
            override fun onResponse(response: JSONObject) {
                Log.d(Logger.LOGTAG, response.toString())
                mCallBack?.onSuccess(getItemListFromJson(response))
            }
        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError) {
                VolleyLog.d(Logger.LOGTAG, "Error: " + error.message)
                Log.e(Logger.LOGTAG, "Site Info Error: " + error.message)
                mCallBack?.onFailure(error)
            }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", GlobalConstants.JWT_PREFIX + " " + currentUser?.jwtToken)
                return headers
            }
        }
    }
}