package com.ai.deep.andy.carrecognizer.utils

import org.json.JSONObject

object ClassifierUtils {

    fun formatClassifierResult(raw: JSONObject): String{
        return raw.toString()
    }
}