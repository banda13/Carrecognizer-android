package com.ai.deep.andy.carrecognizer.utils

import android.util.Log


object ErrorUtils {

    fun logError(message: String, cause: Throwable?, code: ErrorCode?){
        val errorCode = if(code == null){
            ErrorCode.UNKNOWN_ERROR
        }
        else{
            code
        }
        if(cause != null) {
            Log.e(Logger.LOGTAG, errorCode.name + " : " +  message, cause)
        }
    }

    enum class ErrorCode {
        SERVER_TIMEOUT,
        UNKNOWN_ERROR,

        USER_LOGIN_FAILED,
        USER_REGISTRATION_FAILED,
    }
}


