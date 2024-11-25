package com.paydock.sample.feature.card.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class TokeniseCardResponse(
    val resource: Resource<String>,
    val status: Int,
)
