package com.paydock.sample.feature.threeDS.domain.repository

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateMPGS3dsTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken

interface ThreeDSRepository {

    suspend fun createMPGS3dsToken(
        accessToken: String,
        request: CreateMPGS3dsTokenRequest
    ): ThreeDSToken

    suspend fun createStandalone3dsToken(
        accessToken: String,
        request: CreateStandaloneThreeDSTokenRequest
    ): ThreeDSToken

    suspend fun capture3DSCharge(
        accessToken: String,
        request: Capture3DSChargeRequest
    ): ChargeResponse
}