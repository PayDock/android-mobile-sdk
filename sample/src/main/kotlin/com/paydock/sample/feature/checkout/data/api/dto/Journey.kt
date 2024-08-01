package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName

data class Journey(
    val sense: String,
    val title: String,
    val message: String,
    val thread: String,
    val context: String?,
    @SerializedName("_id") val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)
