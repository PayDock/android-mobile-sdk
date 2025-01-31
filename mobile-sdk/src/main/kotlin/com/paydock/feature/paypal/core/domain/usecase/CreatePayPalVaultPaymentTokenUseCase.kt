package com.paydock.feature.paypal.core.domain.usecase

import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.card.domain.repository.CardRepository
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenRequest
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails
import com.paydock.feature.paypal.core.domain.repository.PayPalRepository

/**
 * Use case for creating a one-time transaction (OTT) payment token.
 *
 * This class interacts with the [CardRepository] to create a payment token using the provided access token and
 * request details. The payment token is used for a single transaction.
 *
 * @property repository The repository responsible for creating the OTT payment token.
 */
internal class CreatePayPalVaultPaymentTokenUseCase(private val repository: PayPalRepository) {

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
        request: PayPalVaultTokenRequest
    ): Result<PayPalPaymentTokenDetails> =
        suspendRunCatchingMapper(PayPalVaultException.CreatePaymentTokenException::class) {
            repository.createPaymentToken(accessToken, setupToken, request)
        }
}