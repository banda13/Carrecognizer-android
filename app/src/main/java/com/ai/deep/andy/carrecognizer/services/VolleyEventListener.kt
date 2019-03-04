package com.ai.deep.andy.carrecognizer.services

interface VolleyOnEventListener<in T> {

    fun onSuccess(obj: T)

    fun onFailure(e: Exception)
}