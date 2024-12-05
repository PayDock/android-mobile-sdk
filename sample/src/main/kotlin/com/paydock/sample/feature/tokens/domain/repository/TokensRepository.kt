package com.paydock.sample.feature.tokens.domain.repository

import com.paydock.sample.feature.tokens.data.api.dto.TokeniseCardRequest

interface TokensRepository {
    suspend fun tokeniseCardDetails(accessToken: String, request: TokeniseCardRequest): String
}