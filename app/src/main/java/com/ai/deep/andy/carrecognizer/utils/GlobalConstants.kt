package com.ai.deep.andy.carrecognizer.utils

class GlobalConstants {
    companion object {
        const val USER_ID = "thisistheonlyuserandcantchangethis"

        const val JWT_PREFIX = "andy"

        //const val BASEURL = "http://carrecognizer.northeurope.cloudapp.azure.com/"
        const val BASEURL = "http://192.168.0.185/"
        const val USERS_URL = BASEURL + "users/"
        const val CORE_URL = BASEURL + "core/"

        const val FILES_URL = CORE_URL + "file/"
    }
}