package com.paydock.sample.feature.wallet.domain.repository

import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.domain.model.WalletCharge

interface WalletRepository {
    suspend fun initiateWalletTransaction(
        manualCapture: Boolean,
        request: InitiateWalletRequest
    ): WalletCharge

    suspend fun captureWalletCharge(chargeId: String): WalletCharge
}