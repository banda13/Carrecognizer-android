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


class ListClassificationService(context: Context, callback: VolleyOnEventListener<List<ClassificationItem>>) {

    var context : Context? = context
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<List<ClassificationItem>>? = callback
    //val BASE_URL = "http://carrecognizer.northeurope.cloudapp.azure.com/core/"
    val BASE_URL = "http://176.63.245.216:1235/core/"
    var currentUser : User? = null

    init {
        this.queue = Volley.newRequestQueue(context)
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

        var url = BASE_URL + "classlist/"
        if(page > 1){
            url += "?page=$page"
        }

        val req = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
            Log.d(Logger.LOGTAG, response.toString())
            mCallBack?.onSuccess(getItemListFromJson(response))
        }, Response.ErrorListener { error ->
            VolleyLog.d(Logger.LOGTAG, "Error: " + error.message)
            Log.e(Logger.LOGTAG, "Site Info Error: " + error.message)
            mCallBack?.onFailure(error)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", GlobalConstants.JWT_PREFIX + " " + currentUser?.jwtToken)
                return headers
            }
        }
        queue!!.add(req)
    }

    init {
        this.queue = Volley.newRequestQueue(context)
    }
}