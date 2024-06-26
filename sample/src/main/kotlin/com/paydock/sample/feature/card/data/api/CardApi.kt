package com.paydock.sample.feature.card.data.api

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardResponse
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CardApi {

    @POST("/v1/payment_sources/tokens")
    suspend fun tokeniseCardDetails(
        @Header("x-user-public-key") publicKey: String = BuildConfig.PUBLIC_KEY,
        @Body request: TokeniseCardRequest
    ): TokeniseCardResponse

    @POST("/v1/vault/payment_sources")
    suspend fun createVaultToken(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: VaultTokenRequest.CreateCardVaultTokenRequest
    ): VaultTokenResponse

    @POST("/v1/vault/payment_sources")
    suspend fun createVaultToken(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: VaultTokenRequest.CreateCardSessionVaultTokenRequest
    ): VaultTokenResponse

    @POST("/v1/charges")
    suspend fun captureCharge(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: CaptureCardChargeRequest
    ): ChargeResponse

}