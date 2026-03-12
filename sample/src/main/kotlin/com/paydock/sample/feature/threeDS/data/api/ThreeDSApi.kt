package com.paydock.sample.feature.threeDS.data.api

import com.paydock.sample.feature.checkout.data.api.dto.CaptureChargeResponse
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateMPGS3dsTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.ThreeDSTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ThreeDSApi {

    @POST("/v1/charges/3ds")
    suspend fun createMPGS3dsToken(
        @Header("X-Access-Token") accessToken: String,
        @Body request: CreateMPGS3dsTokenRequest,
    ): ThreeDSTokenResponse

    @POST("/v1/charges/standalone-3ds")
    suspend fun createStandalone3dsToken(
        @Header("X-Access-Token") accessToken: String,
        @Body request: CreateStandaloneThreeDSTokenRequest,
    ): ThreeDSTokenResponse

    @POST("/v1/charges")
    suspend fun capture3DSCharge(
        @Header("X-Access-Token") accessToken: String,
        @Body request: Capture3DSChargeRequest,
    ): CaptureChargeResponse

}