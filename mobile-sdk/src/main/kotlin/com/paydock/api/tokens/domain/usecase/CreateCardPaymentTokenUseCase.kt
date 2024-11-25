package com.paydock.api.tokens.domain.usecase

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.domain.model.TokenDetails
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.extensions.suspendRunCatchingMapper

/**
 * Use case responsible for tokenizing credit card details.
 *
 * This use case interacts with the provided [TokenRepository] to request tokenization
 * of credit card information based on the given [CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class CreateCardPaymentTokenUseCase(private val repository: TokenRepository) {
    /**
     * Invokes the use case to tokenize credit card details.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard] containing credit card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(
        accessToken: String,
        request: CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard
    ): Result<TokenDetails> =
        suspendRunCatchingMapper<TokenDetails, CardDetailsException.TokenisingCardException> {
            repository.createPaymentToken(accessToken, request)
        }
}
