package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.repository.CardDetailsRepository

/**
 * Use case responsible for tokenizing gift card details.
 *
 * This use case interacts with the provided [CardDetailsRepository] to request tokenization
 * of gift card information based on the given [TokeniseCardRequest.GiftCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class TokeniseGiftCardUseCase(private val repository: CardDetailsRepository) {
    /**
     * Invokes the use case to tokenize gift card details.
     *
     * @param request The [TokeniseCardRequest.GiftCard] containing credit card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(request: TokeniseCardRequest.GiftCard): Result<TokenisedCardDetails> =
        suspendRunCatching {
            repository.tokeniseCardDetails(request)
        }
}
