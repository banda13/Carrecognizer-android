package com.ai.deep.andy.carrecognizer.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

object DateUtils {

    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    fun toSimpleString(date: java.util.Date?) : String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return if(date == null) {
            "null"
        }
        else{
            format.format(date)
        }
    }

}