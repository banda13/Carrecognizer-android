package com.ai.deep.andy.carrecognizer.services.statistics

import android.content.Context
import android.util.Log
import com.ai.deep.andy.carrecognizer.model.User
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.android.volley.RequestQueue
import org.json.JSONObject

class AvgClassificationTimeService(context: Context, callback: VolleyOnEventListener<Int>) {

    var context: Context? = context
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<Int>? = callback
    val BASE_URL = "http://176.63.245.216:1235/core/statistics/classification/avgtime"
    var currentUser: User? = null

    fun getAvarageUsageStatistics(){
        Log.i(Logger.LOGTAG, "Getting average classification time")
        mCallBack?.onSuccess(3000)
    }
}