package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName

data class Configuration(
    @SerializedName("template_id") val templateId: String?,
    @SerializedName("payment_method_options") val paymentMethodOptions: PaymentMethodOptions? = null
)