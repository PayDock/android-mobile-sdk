package com.paydock.sample.feature.card.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.card.domain.repository.CardRepository
import javax.inject.Inject

class TokeniseCardUseCase @Inject constructor(private val repository: CardRepository) {

    suspend operator fun invoke(request: TokeniseCardRequest) = suspendRunCatching {
        repository.tokeniseCardDetails(request)
    }
}