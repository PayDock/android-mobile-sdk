package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.runCatchingFlow
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.repository.CardDetailsRepository

internal class TokeniseCreditCardFlowUseCase(private val repository: CardDetailsRepository) {
    operator fun invoke(request: TokeniseCardRequest.CreditCard) = runCatchingFlow {
        repository.tokeniseCardDetailsFlow(request)
    }
}