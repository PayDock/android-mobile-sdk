package com.paydock.core.utils.jwt.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetaX(
    @SerialName("error_url") val errorUrl: String,
    @SerialName("success_url") val successUrl: String
)