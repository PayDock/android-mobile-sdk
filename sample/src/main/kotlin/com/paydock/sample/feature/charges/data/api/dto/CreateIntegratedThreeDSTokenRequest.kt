package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_CURRENCY_CODE
import java.math.BigDecimal

data class CreateIntegratedThreeDSTokenRequest(
    val token: String? = null,
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String = AU_CURRENCY_CODE,
    val customer: ChargesCustomerDTO? = null,
    @SerializedName("_3ds") val threeDSDetails: ThreeDSDetails = ThreeDSDetails(),
    // We do not need any other request properties (ie. customer, shipping)
) {
    data class ThreeDSDetails(
        @SerializedName("browser_details") val browserDetails: BrowserDetails = BrowserDetails(),
    )

    data class BrowserDetails(
        val name: String = "chrome",
        @SerializedName("java_enabled") val javaEnabled: String = "true",
        val language: String = "en-US",
        @SerializedName("screen_height") val screenHeight: String = "640",
        @SerializedName("screen_width") val screenWidth: String = "480",
        @SerializedName("time_zone") val timeZone: String = "273",
        @SerializedName("color_depth") val colorDepth: String = "24",
    )
}