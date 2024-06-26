package com.paydock.core.utils.jwt.models

import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val charge: Charge,
    val gateway: Gateway
)