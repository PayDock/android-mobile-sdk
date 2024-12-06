package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName

data class ThreeDSResourceData(
    @SerializedName("_3ds") val threeDS: ThreeDSData,
    @SerializedName("status") val status: String,
) {
    data class ThreeDSData(
        @SerializedName("id") val id: String,
        // The token will be null/empty if it is not a valid 3DS flow
        @SerializedName("token") val token: String? = null,
        // We do not need any other response properties (ie. charge)
    )
}