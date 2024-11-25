package com.paydock.sample.feature.threeDS.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.feature.wallet.data.api.dto.Customer
import com.paydock.sample.feature.wallet.data.api.dto.PaymentSource

data class CreateStandaloneThreeDSTokenRequest(
    val amount: String = "10",
    val currency: String = AU_CURRENCY_CODE,
    val customer: Customer,
    @SerializedName("_3ds") val threeDSDetails: ThreeDSDetails = ThreeDSDetails(),
    // We do not need any other request properties (ie. customer, shipping)
) {
    data class ThreeDSDetails(
        @SerializedName("service_id") val serviceId: String = BuildConfig.STANDALONE_3DS_SERVICE_ID,
        val authentication: Authentication = Authentication(),
    )

    data class Authentication(
        val type: String = "01",
        val date: String = "2023-11-11T13:00:00.521Z",
        val version: String = "2.2.0",
        val customer: Customer = Customer(
            credentialsUpdatedAt = "2023-05-31T20:06:05.521Z",
            suspicious = false,
            paymentSource = PaymentSource(
                createdAt = "2023-11-11T20:06:05.521Z",
                addAttempts = listOf("2023-11-11T20:06:05.521Z"),
                cardType = "02"
            )
        ),
    )
}