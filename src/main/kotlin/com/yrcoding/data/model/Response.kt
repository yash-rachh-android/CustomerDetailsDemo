package com.yrcoding.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    var status : Int = 200,
    var message : String = "",
    var data : T ?= null
) {
}