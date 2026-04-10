package com.paydock.feature.googlepay.domain.usecase

import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.googlepay.data.dto.CreateGooglePayTokenRequest
import com.paydock.feature.googlepay.domain.repository.GooglePayRepository

/**
 * Use case responsible for creating a payment token from Google Pay payment data.
 *
 * This use case interacts with the provided [GooglePayRepository] to request tokenization
 * of Google Pay payment information.
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class CreateGooglePayTokenUseCase(private val repository: GooglePayRepository) {

    /**
     * Invokes the use case to create a payment token from Google Pay payment data.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [CreateGooglePayTokenRequest] containing the Google Pay token.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(
        accessToken: String,
        request: CreateGooglePayTokenRequest
    ): Result<TokenDetails> =
        suspendRunCatchingMapper(GooglePayException.TokenisingGooglePayException::class) {
            repository.createPaymentToken(accessToken, request)
        }
}
