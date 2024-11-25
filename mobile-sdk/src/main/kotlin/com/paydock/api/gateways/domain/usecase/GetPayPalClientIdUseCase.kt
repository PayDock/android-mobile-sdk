package com.paydock.api.gateways.domain.usecase

import com.paydock.api.gateways.domain.repository.GatewayRepository
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.extensions.suspendRunCatchingMapper

/**
 * Use case for retrieving the PayPal client ID.
 *
 * This use case interacts with the [TokenRepository] to obtain the PayPal client ID
 * associated with a given gateway. The client ID is required for initializing PayPal transactions.
 *
 * @property repository The repository responsible for handling PayPal vault operations.
 */
internal class GetPayPalClientIdUseCase(private val repository: GatewayRepository) {

    /**
     * Invokes the use case to retrieve the PayPal client ID.
     *
     * This function calls the repository to retrieve the client ID for the specified gateway using
     * the provided OAuth token for authentication. The result is wrapped in a [Result] to handle
     * success or failure.
     *
     * @param accessToken The OAuth token used for authenticating the request.
     * @param gatewayId The identifier for the payment gateway.
     * @return A [Result] containing the PayPal client ID or an error.
     */
    suspend operator fun invoke(
        accessToken: String,
        gatewayId: String
    ): Result<String> =
        suspendRunCatchingMapper<String, PayPalVaultException.GetPayPalClientIdException> {
            repository.getGatewayClientId(accessToken, gatewayId)
        }
}
