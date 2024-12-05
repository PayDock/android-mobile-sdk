package com.paydock.sample.feature.tokens.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.tokens.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.tokens.domain.repository.TokensRepository
import javax.inject.Inject

class TokeniseCardUseCase @Inject constructor(private val repository: TokensRepository) {

    suspend operator fun invoke(accessToken: String, request: TokeniseCardRequest) =
        suspendRunCatching {
            repository.tokeniseCardDetails(accessToken, request)
        }
}