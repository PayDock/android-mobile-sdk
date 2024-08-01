package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.runCatchingFlow
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.repository.CardDetailsRepository

/**
 * Use case responsible for tokenizing credit card details using Flow.
 *
 * This use case interacts with the provided [CardDetailsRepository] to request tokenization
 * of credit card information based on the given [TokeniseCardRequest.CreditCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class TokeniseCreditCardFlowUseCase(private val repository: CardDetailsRepository) {

    /**
     * Invokes the use case to tokenize credit card details.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [TokeniseCardRequest.CreditCard] containing credit card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    operator fun invoke(accessToken: String, request: TokeniseCardRequest.CreditCard) = runCatchingFlow {
        repository.tokeniseCardDetailsFlow(accessToken, request)
    }
}