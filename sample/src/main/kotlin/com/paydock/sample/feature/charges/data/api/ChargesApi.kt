package com.paydock.sample.feature.charges.data.api

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.charges.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.charges.data.api.dto.CaptureChargeResponse
import com.paydock.sample.feature.charges.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.charges.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.charges.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.charges.data.api.dto.ThreeDSTokenResponse
import com.paydock.sample.feature.charges.data.api.dto.WalletCaptureResponse
import com.paydock.sample.feature.charges.data.api.dto.WalletInitiateResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ChargesApi {

    @POST("/v1/charges")
    suspend fun captureCharge(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: CaptureCardChargeRequest,
    ): CaptureChargeResponse

    @POST("/v1/charges/wallet")
    suspend fun initiateWalletTransaction(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: InitiateWalletRequest,
    ): WalletInitiateResponse

    @POST("/v1/charges/wallet?capture=false")
    suspend fun initiateWalletTransactionManualCapture(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: InitiateWalletRequest,
    ): WalletInitiateResponse

    @POST("/v1/charges/{id}/capture")
    suspend fun captureWalletCharge(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Path("id") id: String,
    ): WalletCaptureResponse

    @POST("/v1/charges/3ds")
    suspend fun createIntegrated3dsToken(
        @Header("X-Access-Token") accessToken: String,
        @Body request: CreateIntegratedThreeDSTokenRequest,
    ): ThreeDSTokenResponse

    @POST("/v1/charges/standalone-3ds")
    suspend fun createStandalone3dsToken(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: CreateStandaloneThreeDSTokenRequest,
    ): ThreeDSTokenResponse

}