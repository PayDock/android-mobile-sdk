package com.paydock.sample.feature.charges.domain.repository

import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.charges.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.charges.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.charges.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.charges.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.charges.domain.model.ThreeDSToken
import com.paydock.sample.feature.charges.domain.model.WalletCharge
import com.paydock.sample.feature.tokens.data.api.dto.Capture3DSChargeRequest

interface ChargesRepository {
    suspend fun captureCardCharge(request: CaptureCardChargeRequest): ChargeResponse

    suspend fun initiateWalletTransaction(
        manualCapture: Boolean,
        request: InitiateWalletRequest,
    ): WalletCharge

    suspend fun captureWalletCharge(chargeId: String): WalletCharge

    suspend fun createIntegrated3dsToken(
        accessToken: String,
        request: CreateIntegratedThreeDSTokenRequest,
    ): ThreeDSToken

    suspend fun createStandalone3dsToken(request: CreateStandaloneThreeDSTokenRequest): ThreeDSToken

    suspend fun capture3DSCharge(request: Capture3DSChargeRequest): ChargeResponse
}