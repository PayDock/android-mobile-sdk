package com.paydock.feature.card.domain.usecase

import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.repository.CardRepository

/**
 * Use case responsible for tokenizing gift card details.
 *
 * This use case interacts with the provided [CardRepository] to request tokenization
 * of gift card information based on the given [CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class CreateGiftCardPaymentTokenUseCase(private val repository: CardRepository) {
    /**
     * Invokes the use case to tokenize gift card details.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard] containing credit
     * card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(
        accessToken: String,
        request: CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard,
    ): Result<TokenDetails> =
        suspendRunCatchingMapper(GiftCardException.TokenisingCardException::class) {
            repository.createPaymentToken(accessToken, request)
        }
}
