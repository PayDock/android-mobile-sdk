package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.repository.WalletRepository

/**
 * A use case responsible for declining wallet transactions.
 *
 * @param repository The [WalletRepository] that provides data access for declining transactions.
 */
internal class DeclineWalletChargeUseCase(private val repository: WalletRepository) {

    /**
     * Invoke the use case to decline a wallet transaction with the provided chargeId.
     *
     * @param token The authentication token required for the transaction.
     * @param chargeId The chargeId for the transaction.
     * @return A result representing the success or failure of the transaction decline.
     */
    suspend operator fun invoke(token: String, chargeId: String): Result<ChargeResponse> =
        suspendRunCatching {
            repository.declineWalletCharge(token, chargeId)
        }
}
