package com.yrcoding.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UploadImage(
    var fileName : String = "",
    var url : String = ""
) {
}