package com.paydock.sample.feature.threeDS.data.api

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.ThreeDSTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ThreeDSApi {

    @POST("/v1/charges/3ds")
    suspend fun createIntegrated3dsToken(
        @Header("x-user-public-key") publicKey: String = BuildConfig.PUBLIC_KEY,
        @Body request: CreateIntegratedThreeDSTokenRequest
    ): ThreeDSTokenResponse

    @POST("/v1/charges/standalone-3ds")
    suspend fun createStandalone3dsToken(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: CreateStandaloneThreeDSTokenRequest
    ): ThreeDSTokenResponse

}