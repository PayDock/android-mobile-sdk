package com.paydock.sample.feature.tokens.data.api

import com.paydock.sample.feature.tokens.data.api.dto.PaymentOTTResponse
import com.paydock.sample.feature.tokens.data.api.dto.TokeniseCardRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TokensApi {

    @POST("/v1/payment_sources/tokens")
    suspend fun tokeniseCardDetails(
        @Header("X-Access-Token") accessToken: String,
        @Body request: TokeniseCardRequest,
    ): PaymentOTTResponse

}