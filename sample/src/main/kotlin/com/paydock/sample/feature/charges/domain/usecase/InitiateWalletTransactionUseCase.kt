package com.paydock.sample.feature.charges.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.charges.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
import javax.inject.Inject

class InitiateWalletTransactionUseCase @Inject constructor(private val repository: ChargesRepository) {

    suspend operator fun invoke(manualCapture: Boolean = false, request: InitiateWalletRequest) =
        suspendRunCatching {
            repository.initiateWalletTransaction(manualCapture, request)
        }
}