package com.ai.deep.andy.carrecognizer.model

import android.annotation.SuppressLint
import android.util.Log
import com.ai.deep.andy.carrecognizer.utils.ClassifierUtils
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants
import com.ai.deep.andy.carrecognizer.utils.GlobalConstants.Companion.maxResults
import com.ai.deep.andy.carrecognizer.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

class ClassificationItem {

    companion object {

        @SuppressLint("SimpleDateFormat")
        var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")

        fun getItemListFromJson(json : JSONObject): MutableList<ClassificationItem> {
            val items : MutableList<ClassificationItem> = mutableListOf()
            val jsonArray: JSONArray = json.getJSONArray("results")
            for (i in 0..(jsonArray.length() - 1)) {
                items.add(ClassificationItem(jsonArray.getJSONObject(i)))
            }
            return items
        }
    }

    var classificationId : Long? = null
    var image_id : Int? = null
    var img_url : String? = null
    var image_name : String? = null
    var image_extension : String? = null
    var creationDate : Date? = null
    var results : JSONArray? = null
    var creatorUserName : String? = null

    constructor()

    constructor(json: JSONObject){
        this.classificationId = json.getLong("id")

        this.image_id = json.getJSONObject("image").getInt("id")
        this.image_name = json.getJSONObject("image").getString("file_name")
        this.image_extension = json.getJSONObject("image").getString("mime_type")

        this.creatorUserName = json.getJSONObject("creator").getString("username")

        this.img_url = GlobalConstants.FILES_URL + creatorUserName + "/" + image_id + "_" + image_name + image_extension
        this.creationDate = sdf.parse(json.getString("created_at"))

        this.results = json.getJSONArray("_results")
    }

    fun getFormattedResults(): String{
        val builder = StringBuilder()

        if(results?.length() == 0){
            Log.e(Logger.LOGTAG, "Results size is 0")
            return "No result"
        }
        val result = results?.getJSONObject(0)?.getJSONArray("_items")
        for (i in 0 until maxResults) {
            val item = result?.getJSONObject(i)
            builder.append(item?.optString("name") + " - " + String.format("%.3f", item?.optDouble("accuracy", 0.0)) + "\n")
        }

        return builder.toString()
    }
}