package com.paydock.sample.feature.card.data.api

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.PaymentOTTResponse
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenResponse
import com.paydock.sample.feature.checkout.data.api.dto.CaptureChargeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CardApi {

    @POST("/v1/payment_sources/tokens")
    suspend fun tokeniseCardDetails(
        @Header("X-Access-Token") accessToken: String = BuildConfig.ACCESS_TOKEN_WIDGET,
        @Body request: TokeniseCardRequest,
    ): PaymentOTTResponse

    @POST("/v1/charges")
    suspend fun captureCharge(
        @Header("X-Access-Token") accessToken: String = BuildConfig.ACCESS_TOKEN_API,
        @Body request: CaptureCardChargeRequest,
    ): CaptureChargeResponse

    @POST("/v1/vault/payment_sources")
    suspend fun createVaultToken(
        @Header("X-Access-Token") accessToken: String = BuildConfig.ACCESS_TOKEN_API,
        @Body request: VaultTokenRequest,
    ): VaultTokenResponse
}