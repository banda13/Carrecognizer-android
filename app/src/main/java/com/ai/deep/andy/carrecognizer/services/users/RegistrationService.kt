package com.ai.deep.andy.carrecognizer.services.users

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.services.CRServerException
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class RegistrationService {

    var context : Context? = null
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<JSONObject>? = null
    //val BASE_URL = "http://carrecognizer.northeurope.cloudapp.azure.com/users/"
    val BASE_URL = "http://176.63.245.216:1235/users/"

    constructor(context: Context, callback: VolleyOnEventListener<JSONObject>){
        this.context = context
        this.queue = Volley.newRequestQueue(context)
        this.mCallBack = callback
    }

    fun registration(email: String, password: String, first_name: String, last_name: String){
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password
        params["first_name"] = first_name
        params["last_name"] = last_name

        val queue = Volley.newRequestQueue(context)
        val postRequest = object : JsonObjectRequest(BASE_URL + "create/", JSONObject(params),
                Response.Listener<JSONObject> { response ->
                    Log.i("Response", response.toString())
                    if(response.has("error")){
                        mCallBack?.onFailure(CRServerException(response.optString("error")))
                    }
                    else {
                        mCallBack?.onSuccess(response)
                    }
                },
                Response.ErrorListener { error ->
                    mCallBack?.onFailure(error)
                }
        ) {

        }
        queue!!.add(postRequest)
    }
}