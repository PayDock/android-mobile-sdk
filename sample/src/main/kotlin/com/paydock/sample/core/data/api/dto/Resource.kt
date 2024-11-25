package com.paydock.sample.core.data.api.dto

import com.google.gson.annotations.SerializedName

data class Resource<T>(
    @SerializedName("data") val resourceData: T,
    val type: String,
)