package com.yrcoding.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    var id: Int ?= null,
    var email: String ?= null,
    var firstName: String ?= null,
    var lastName: String ?= null,
    var city: String ?= null,
    var mobile: String ?= null,
    var password: String ?= null,
    var profile: String ?= null
) {
}