package com.paydock.sample.feature.wallet.data.api

import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.sample.feature.wallet.data.api.dto.WalletInitiateResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletApi {

    @POST("/v1/charges/wallet")
    suspend fun initiateWalletTransaction(
        @Header("X-Access-Token") accessToken: String,
        @Body request: InitiateWalletRequest,
    ): WalletInitiateResponse

    @POST("/v1/charges/wallet?capture=false")
    suspend fun initiateWalletTransactionManualCapture(
        @Header("X-Access-Token") accessToken: String,
        @Body request: InitiateWalletRequest,
    ): WalletInitiateResponse

    @POST("/v1/charges/{id}/capture")
    suspend fun captureWalletCharge(
        @Header("X-Access-Token") accessToken: String,
        @Path("id") id: String,
    ): WalletCaptureResponse

}