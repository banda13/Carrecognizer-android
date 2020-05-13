package com.ai.deep.andy.carrecognizer.services.users

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.model.User
import com.ai.deep.andy.carrecognizer.services.CRServerException
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.orm.SugarRecord
import org.json.JSONObject

class LoginService {

    var context : Context? = null
    var queue: RequestQueue? = null
    var mCallBack: VolleyOnEventListener<JSONObject>? = null
    //val BASE_URL = "http://carrecognizer.northeurope.cloudapp.azure.com/users/"
    val BASE_URL = "http://178.48.246.170:1235/users/"

    constructor(context: Context,callback: VolleyOnEventListener<JSONObject>){
        this.context = context
        this.queue = Volley.newRequestQueue(context)
        this.mCallBack = callback
    }

    fun updateUserToken(token: String){
        //there will be maximum 1 User
        val users = SugarRecord.findAll(User::class.java)
        if(users.hasNext()){
            val u : User = users.next()
            u.jwtToken = token
            u.save()
            Log.i(Logger.LOGTAG, "User JWT token updated!")
        }
    }

    fun login(email: String, password: String){

        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password

        Log.i(Logger.LOGTAG, "Loggin in with email " + email + " and password: " + password)

        val postRequest = object : JsonObjectRequest(BASE_URL + "login/", JSONObject(params),
                Response.Listener<JSONObject> { response ->
                    Log.i("Response", response.toString())
                    if(response.has("error")){
                        mCallBack?.onFailure(CRServerException(response.optString("error")))
                    }
                    else {
                        updateUserToken(response["token"] as String)
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