package com.paydock.api.tokens.domain.usecase

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.domain.model.TokenDetails
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.extensions.suspendRunCatching

/**
 * Use case responsible for tokenizing gift card details.
 *
 * This use case interacts with the provided [TokenRepository] to request tokenization
 * of gift card information based on the given [CreatePaymentTokenRequest.TokeniseCardRequest.GiftCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class CreateGiftCardPaymentTokenUseCase(private val repository: TokenRepository) {
    /**
     * Invokes the use case to tokenize gift card details.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [CreatePaymentTokenRequest.TokeniseCardRequest.GiftCard] containing credit
     * card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(
        accessToken: String,
        request: CreatePaymentTokenRequest.TokeniseCardRequest.GiftCard
    ): Result<TokenDetails> =
        suspendRunCatching {
            repository.createPaymentToken(accessToken, request)
        }
}
