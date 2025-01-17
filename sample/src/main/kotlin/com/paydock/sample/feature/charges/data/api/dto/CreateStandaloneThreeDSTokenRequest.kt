package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_CURRENCY_CODE
import java.math.BigDecimal

data class CreateStandaloneThreeDSTokenRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String = AU_CURRENCY_CODE,
    val customer: ChargesCustomerDTO,
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
        val customer: ChargesCustomerDTO = ChargesCustomerDTO(
            credentialsUpdatedAt = "2023-05-31T20:06:05.521Z",
            suspicious = false,
            paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                createdAt = "2023-11-11T20:06:05.521Z",
                addAttempts = listOf("2023-11-11T20:06:05.521Z"),
                cardType = "02"
            )
        ),
    )
}