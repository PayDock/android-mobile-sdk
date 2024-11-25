package com.paydock.api.tokens.domain.usecase

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.domain.model.PayPalPaymentTokenDetails
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.extensions.suspendRunCatchingMapper

/**
 * Use case for creating a one-time transaction (OTT) payment token.
 *
 * This class interacts with the [TokenRepository] to create a payment token using the provided access token and
 * request details. The payment token is used for a single transaction.
 *
 * @property repository The repository responsible for creating the OTT payment token.
 */
internal class CreatePayPalVaultPaymentTokenUseCase(private val repository: TokenRepository) {

    /**
     * Invokes the use case to create a payment token.
     *
     * @param accessToken The access token for authenticating the request.
     * @param request The request object containing the gateway ID and setup token.
     * @return A [Result] object containing the payment token as a String, or an error if the token creation fails.
     */
    suspend operator fun invoke(
        accessToken: String,
        setupToken: String,
        request: CreatePaymentTokenRequest.PayPalVaultRequest
    ): Result<PayPalPaymentTokenDetails> =
        suspendRunCatchingMapper<PayPalPaymentTokenDetails, PayPalVaultException.CreatePaymentTokenException> {
            repository.createPaymentToken(accessToken, setupToken, request)
        }
}