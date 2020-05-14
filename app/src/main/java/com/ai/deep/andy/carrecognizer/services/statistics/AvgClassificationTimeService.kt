package com.ai.deep.andy.carrecognizer.services.statistics

import android.content.Context
import android.util.Log
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
import kotlin.math.roundToInt

class AvgClassificationTimeService(context: Context, callback: VolleyOnEventListener<Int>) {

    var context: Context? = context
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<Int>? = callback
    val BASE_URL = "http://178.48.246.170:1235/stats/"
    var currentUser: User? = null

    private fun getUser(): User? {
        val users = SugarRecord.findAll(User::class.java)
        if (users.hasNext()) {
            return users.next()
        }
        return null
    }

    init {
        this.queue = Volley.newRequestQueue(context)
    }

    fun getAvarageUsageStatistics(){
        Log.i(Logger.LOGTAG, "Getting average classification time")

        currentUser = getUser()

        val url = BASE_URL + "classification_time/"
        val req = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener<JSONObject> { response ->
            Log.d(Logger.LOGTAG, response.toString())
            val avgTime = response.getDouble("avg_class_time").roundToInt() * 4 * 1000 // change to ms + much slower so multiply with 4
            mCallBack?.onSuccess(avgTime)
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