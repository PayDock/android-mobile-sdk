package com.paydock.sample.feature.charges.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
import javax.inject.Inject

class CaptureWalletChargeUseCase @Inject constructor(private val repository: ChargesRepository) {

    suspend operator fun invoke(chargeId: String) =
        suspendRunCatching {
            repository.captureWalletCharge(chargeId)
        }
}