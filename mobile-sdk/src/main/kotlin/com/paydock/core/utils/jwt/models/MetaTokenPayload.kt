package com.paydock.core.utils.jwt.models

import kotlinx.serialization.Serializable

@Serializable
data class MetaTokenPayload(
    val meta: Meta
)