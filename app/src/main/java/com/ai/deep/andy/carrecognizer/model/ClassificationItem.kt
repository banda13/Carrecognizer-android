package com.ai.deep.andy.carrecognizer.model

import com.ai.deep.andy.carrecognizer.utils.GlobalConstants
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

class ClassificationItem {

    companion object {
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
    var results : JSONObject? = null
    var creatorUserName : String? = null

    constructor()

    constructor(json: JSONObject){
        this.classificationId = json.getLong("id")

        this.image_id = json.getJSONObject("image").getInt("id")
        this.image_name = json.getJSONObject("image").getString("file_name")
        this.image_extension = json.getJSONObject("image").getString("mime_type")

        this.creatorUserName = json.getJSONObject("creator").getString("username")

        this.img_url = GlobalConstants.FILES_URL + creatorUserName + "/" + image_id + "_" + image_name + image_extension
        this.creationDate = null //TODO

        this.results = json.getJSONObject("_results")
    }

    fun getFormattedResults(): String{
        //TODO format this
        return this.results.toString()
    }
}