package com.paydock.sample.feature.wallet.domain.repository

import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.model.WalletCharge

interface WalletRepository {

    suspend fun initiateWalletTransaction(
        accessToken: String,
        manualCapture: Boolean,
        request: InitiateWalletRequest,
    ): WalletCharge

    suspend fun captureWalletCharge(
        accessToken: String,
        chargeId: String,
    ): WalletCharge
}