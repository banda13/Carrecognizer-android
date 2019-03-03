package com.ai.deep.andy.carrecognizer.model

import com.orm.SugarRecord

class User : SugarRecord<User> {

    constructor() : super()

    constructor(username: String?, email: String?, first_name: String?, last_name: String?, last_login: Long?, password: String?) : super() {
        this.username = username
        this.email = email
        this.first_name = first_name
        this.last_name = last_name
        this.registration_date = last_login
        this.password = password
        this.temporary_user = false
    }

    constructor(temporary_email: String?, temporary_password: String?) : super() {
        this.temporary_user = true
        this.temporary_email = temporary_email
        this.temporary_password = temporary_password
    }

    var username : String? = null
    var email : String? = null
    var first_name : String? = null
    var last_name : String? = null
    var registration_date : Long? = null
    var password : String? = null
    
    var temporary_user : Boolean = true
    var temporary_email : String? = null
    var temporary_password : String? = null
    
}