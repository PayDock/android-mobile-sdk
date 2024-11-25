package com.paydock.api.charges.domain.usecase

import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.repository.ChargeRepository
import com.paydock.core.extensions.suspendRunCatching

/**
 * A use case for fetching wallet callback information.
 *
 * @param repository The repository responsible for interacting with wallet callback data.
 */
internal class GetWalletCallbackUseCase(private val repository: ChargeRepository) {
    /**
     * Invoke the use case to fetch wallet callback data.
     *
     * @param token The authentication token for accessing wallet callback data.
     * @param request The request object specifying the details of the wallet callback data to fetch.
     *
     * @return A [Result] representing the outcome of the wallet callback data retrieval.
     */
    suspend operator fun invoke(
        token: String,
        request: WalletCallbackRequest
    ): Result<WalletCallback> =
        suspendRunCatching {
            repository.getWalletCallback(token, request)
        }
}
