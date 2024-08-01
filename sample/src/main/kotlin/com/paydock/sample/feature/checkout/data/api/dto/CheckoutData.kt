package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName

data class CheckoutData(
    @SerializedName("_id") val id: String,
    val version: String,
    val status: String,
    val reason: String?,
    val amount: String,
    val currency: String,
    val reference: String?,
    val description: String?,
    val journey: List<Journey>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val token: String
)