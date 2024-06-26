package com.paydock.core.utils.jwt.models

import kotlinx.serialization.Serializable

@Serializable
data class Gateway(
    val mode: String,
    val type: String
)