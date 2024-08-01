package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName

data class Customisation(
    @SerializedName("template_id") val templateId: String?,
    val base: Base?
)