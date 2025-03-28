package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.runCatchingFlow
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.repository.CardRepository

/**
 * Use case responsible for tokenizing credit card details using Flow.
 *
 * This use case interacts with the provided [CardRepository] to request tokenization
 * of credit card information based on the given [CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class CreateCardPaymentTokenFlowUseCase(private val repository: CardRepository) {

    /**
     * Invokes the use case to tokenize credit card details.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard] containing credit card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    operator fun invoke(
        accessToken: String,
        request: CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard
    ) = runCatchingFlow {
        repository.createPaymentTokenFlow(accessToken, request)
    }
}