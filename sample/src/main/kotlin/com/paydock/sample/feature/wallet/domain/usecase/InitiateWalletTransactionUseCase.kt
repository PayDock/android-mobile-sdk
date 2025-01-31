package com.paydock.sample.feature.wallet.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class InitiateWalletTransactionUseCase @Inject constructor(private val repository: WalletRepository) {

    suspend operator fun invoke(manualCapture: Boolean = false, request: InitiateWalletRequest) =
        suspendRunCatching {
            repository.initiateWalletTransaction(manualCapture, request)
        }
}