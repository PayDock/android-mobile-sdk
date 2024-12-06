package com.paydock.sample.feature.vaults.data.api

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VaultsApi {

    @POST("/v1/vault/payment_sources")
    suspend fun createVaultToken(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: VaultTokenRequest,
    ): VaultTokenResponse

}