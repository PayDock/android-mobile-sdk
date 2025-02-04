package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.repository.WalletRepository

/**
 * A use case for fetching wallet callback information.
 *
 * @param repository The repository responsible for interacting with wallet callback data.
 */
internal class GetWalletCallbackUseCase(private val repository: WalletRepository) {
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
