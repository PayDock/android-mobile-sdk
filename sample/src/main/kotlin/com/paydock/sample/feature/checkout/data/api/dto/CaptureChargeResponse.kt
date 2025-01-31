package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.data.api.dto.Resource

data class CaptureChargeResponse(
    val error: Any,
    val resource: Resource<ChargeDTO>,
    val status: Int,
) {
    data class ChargeDTO(
        @SerializedName("_id") val id: String,
        val status: String,
        // We do not need any other response properties (ie. charge details)
    )
}