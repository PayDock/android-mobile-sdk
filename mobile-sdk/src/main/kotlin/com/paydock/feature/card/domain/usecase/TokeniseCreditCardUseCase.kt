package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.repository.CardDetailsRepository

/**
 * Use case responsible for tokenizing credit card details.
 *
 * This use case interacts with the provided [CardDetailsRepository] to request tokenization
 * of credit card information based on the given [TokeniseCardRequest.CreditCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class TokeniseCreditCardUseCase(private val repository: CardDetailsRepository) {
    /**
     * Invokes the use case to tokenize credit card details.
     *
     * @param request The [TokeniseCardRequest.CreditCard] containing credit card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(request: TokeniseCardRequest.CreditCard): Result<TokenisedCardDetails> =
        suspendRunCatching {
            repository.tokeniseCardDetails(request)
        }
}
