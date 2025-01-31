package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.repository.WalletRepository

/**
 * A use case responsible for capturing wallet transactions.
 *
 * @param repository The [WalletRepository] that provides data access for capturing transactions.
 */
internal class CaptureWalletChargeUseCase(private val repository: WalletRepository) {

    /**
     * Invoke the use case to capture a wallet transaction with the provided token and request details.
     *
     * @param token The authentication token required for the transaction.
     * @param request The request object containing transaction details.
     * @return A result representing the success or failure of the transaction capture.
     */
    suspend operator fun invoke(
        token: String,
        request: CaptureWalletChargeRequest
    ): Result<ChargeResponse> = suspendRunCatching {
        repository.captureWalletTransaction(token, request)
    }
}