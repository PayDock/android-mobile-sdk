package com.paydock.feature.paypal.core.domain.usecase

import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.paypal.core.data.dto.CreateSetupTokenRequest
import com.paydock.feature.paypal.core.domain.repository.PayPalRepository

/**
 * Use case for creating a PayPal setup token for payment sources.
 *
 * This use case encapsulates the logic to create a setup token by interacting with the
 * PayPalVaultRepository. The setup token is required for adding or managing PayPal payment sources.
 *
 * @property repository The repository responsible for handling PayPal vault operations.
 */
internal class CreateSetupTokenUseCase(private val repository: PayPalRepository) {

    /**
     * Invokes the use case to create a setup token.
     *
     * This function calls the repository to create a setup token using the provided OAuth token and request data.
     * It returns the result as a [Result] containing the setup token, or an error if the operation fails.
     *
     * @param accessToken The OAuth token used for authenticating the request.
     * @param request The request object containing the necessary parameters for creating the setup token.
     * @return A [Result] containing the setup token or an error.
     */
    suspend operator fun invoke(
        accessToken: String,
        request: CreateSetupTokenRequest
    ): Result<String> =
        suspendRunCatchingMapper(PayPalVaultException.CreateSetupTokenException::class) {
            repository.createSetupToken(accessToken, request)
        }
}
