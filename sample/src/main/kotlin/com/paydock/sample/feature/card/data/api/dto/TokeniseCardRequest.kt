package com.paydock.sample.feature.card.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.BuildConfig

data class TokeniseCardRequest(
    @SerializedName("card_ccv") val cvv: String = "123",
    @SerializedName("card_name") val cardholderName: String = "Carlie Kuvalis",
    @SerializedName("card_number") val cardNumber: String = "2223000000000007",
    @SerializedName("expire_month") val expiryMonth: String = "08",
    @SerializedName("expire_year") val expiryYear: String = "29",
    @SerializedName("gateway_id") val gatewayId: String = BuildConfig.GATEWAY_ID
)