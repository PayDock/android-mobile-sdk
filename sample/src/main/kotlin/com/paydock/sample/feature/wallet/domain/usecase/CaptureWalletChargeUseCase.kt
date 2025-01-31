package com.paydock.sample.feature.wallet.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class CaptureWalletChargeUseCase @Inject constructor(private val repository: WalletRepository) {

    suspend operator fun invoke(chargeId: String) =
        suspendRunCatching {
            repository.captureWalletCharge(chargeId)
        }
}