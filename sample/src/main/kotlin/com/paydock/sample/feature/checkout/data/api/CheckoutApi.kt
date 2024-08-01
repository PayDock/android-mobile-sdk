package com.paydock.sample.feature.checkout.data.api

import com.paydock.sample.feature.checkout.data.api.dto.CheckoutIntentResponse
import com.paydock.sample.feature.checkout.data.api.dto.CreateIntentRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CheckoutApi {
    @POST("/v1/checkouts/intent")
    suspend fun createCheckoutIntentToken(
        @Header("X-Access-Token") accessToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY2MWU2MWUzN2UzN2M3MTg3ZmVmNzNlYSIsIm1ldGEiOm51bGwsImlhdCI6MTcxMzI2NzE3MX0.CW0Di9M_39IoBU9wqVtskwFfCajw77_zCG1mj3fDZzU",
        @Body request: CreateIntentRequest
    ): CheckoutIntentResponse
}