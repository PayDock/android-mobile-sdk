package com.paydock.feature.paypal.core.domain.usecase

import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.wallet.domain.repository.WalletRepository

/**
 * Use case for retrieving the PayPal client ID.
 *
 * This use case interacts with the [WalletRepository] to obtain the PayPal client ID
 * associated with a given gateway. The client ID is required for initializing PayPal transactions.
 *
 * @property repository The repository responsible for handling PayPal vault operations.
 */
internal class GetPayPalClientIdUseCase(private val repository: WalletRepository) {

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
        suspendRunCatchingMapper(PayPalVaultException.GetPayPalClientIdException::class) {
            repository.getWalletGatewayClientId(accessToken, gatewayId)
        }
}
