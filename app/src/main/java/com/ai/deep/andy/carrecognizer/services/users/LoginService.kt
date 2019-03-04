package com.ai.deep.andy.carrecognizer.services.users

import android.content.Context
import android.util.Log
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class LoginService {

    var context : Context? = null
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<JSONObject>? = null
    val BASE_URL = "http://carrecognizer.northeurope.cloudapp.azure.com/users/"

    constructor(context: Context,callback: VolleyOnEventListener<JSONObject>){
        this.context = context
        this.queue = Volley.newRequestQueue(context)
        this.mCallBack = callback
    }


    fun login(email: String, password: String){

        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password

        val postRequest = object : JsonObjectRequest(BASE_URL + "login/", JSONObject(params),
                Response.Listener<JSONObject> { response ->
                    Log.i("Response", response.toString())
                },
                Response.ErrorListener { error ->
                    Log.i("Error.Response", error.message)
                }
        ) {

        }
        queue!!.add(postRequest)
    }

}