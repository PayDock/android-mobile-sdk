package com.paydock.api.charges.domain.usecase

import com.paydock.api.charges.data.dto.CaptureWalletChargeRequest
import com.paydock.api.charges.domain.repository.ChargeRepository
import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.charge.domain.model.integration.ChargeResponse

/**
 * A use case responsible for capturing wallet transactions.
 *
 * @param repository The [ChargeRepository] that provides data access for capturing transactions.
 */
internal class CaptureWalletChargeUseCase(private val repository: ChargeRepository) {

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
