package com.ai.deep.andy.carrecognizer.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

object DateUtils {

    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    fun toSimpleString(date: java.util.Date?) : String {
        val format = SimpleDateFormat("dd/MM/yyy")
        if(date == null) {
            return "null"
        }
        else{
            return format.format(date)
        }
    }

}