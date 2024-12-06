package com.paydock.sample.feature.customer.data.api

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.customer.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.customer.data.api.dto.CustomerResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CustomerApi {

    @POST("/v1/customers")
    suspend fun createCustomer(
        @Header("x-user-secret-key") secretKey: String = BuildConfig.SECRET_KEY,
        @Body request: CreateCustomerOTTRequest,
    ): CustomerResponse

}