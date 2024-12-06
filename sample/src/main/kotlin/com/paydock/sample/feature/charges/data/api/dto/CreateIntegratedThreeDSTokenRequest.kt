package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName

data class CreateIntegratedThreeDSTokenRequest(
    val token: String? = null,
    val amount: String = "10",
    val currency: String = "AUD",
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