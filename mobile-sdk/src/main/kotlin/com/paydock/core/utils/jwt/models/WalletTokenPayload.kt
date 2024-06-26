package com.paydock.core.utils.jwt.models

import kotlinx.serialization.Serializable

@Serializable
data class WalletTokenPayload(
    val exp: Int,
    val iat: Int,
    val id: String,
    val meta: String
)