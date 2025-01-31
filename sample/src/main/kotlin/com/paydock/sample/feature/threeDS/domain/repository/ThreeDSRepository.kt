package com.paydock.sample.feature.threeDS.domain.repository

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken

interface ThreeDSRepository {

    suspend fun createIntegrated3dsToken(
        accessToken: String,
        request: CreateIntegratedThreeDSTokenRequest
    ): ThreeDSToken

    suspend fun createStandalone3dsToken(request: CreateStandaloneThreeDSTokenRequest): ThreeDSToken

    suspend fun capture3DSCharge(request: Capture3DSChargeRequest): ChargeResponse
}