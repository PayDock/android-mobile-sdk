package com.paydock.sample.feature.customer.data.api.dto

import com.google.gson.annotations.SerializedName

data class ServiceDTO(
    @SerializedName("default_gateway_id") val defaultGatewayId: String,
)