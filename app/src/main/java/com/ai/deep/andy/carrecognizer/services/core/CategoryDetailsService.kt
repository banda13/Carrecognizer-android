package com.ai.deep.andy.carrecognizer.services.core

import android.content.Context
import android.util.Log
import com.ai.deep.andy.carrecognizer.model.ClassificationItem
import com.ai.deep.andy.carrecognizer.model.User
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.orm.SugarRecord
import org.json.JSONObject

class CategoryDetailsService(context: Context, callback: VolleyOnEventListener<JSONObject>) {

    var context: Context? = context
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<JSONObject>? = callback
    val BASE_URL = "http://176.63.245.216:1235/core/"
    var currentUser: User? = null

    init {
        this.queue = Volley.newRequestQueue(context)
    }

    private fun getUser(): User? {
        val users = SugarRecord.findAll(User::class.java)
        if (users.hasNext()) {
            return users.next()
        }
        return null
    }

    fun getDetails(name: String?) {
        currentUser = getUser()

        val url = BASE_URL + "category/" + name
        val req = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
            Log.d(Logger.LOGTAG, response.toString())
            mCallBack?.onSuccess(response)
        }, Response.ErrorListener { error ->
            VolleyLog.d(Logger.LOGTAG, "Error: " + error.message)
            Log.e(Logger.LOGTAG, "Classification details Error: " + error.message)
            mCallBack?.onFailure(error)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = GlobalConstants.JWT_PREFIX + " " + currentUser?.jwtToken
                return headers
            }
        }
        queue!!.add(req)
    }
}