package com.paydock.sample.feature.tokens.data.repository

import com.paydock.sample.feature.tokens.data.api.TokensApi
import com.paydock.sample.feature.tokens.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.tokens.domain.repository.TokensRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TokensRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val tokensApi: TokensApi,
) : TokensRepository {

    override suspend fun tokeniseCardDetails(
        accessToken: String,
        request: TokeniseCardRequest,
    ): String =
        withContext(dispatcher) {
            tokensApi.tokeniseCardDetails(
                accessToken = accessToken,
                request = request
            ).resource.resourceData
        }
}