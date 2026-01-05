package com.paydock.sample.feature.zip.data.api

import com.paydock.sample.feature.checkout.data.api.dto.CaptureChargeResponse
import com.paydock.sample.feature.zip.data.api.dto.CaptureZipChargeRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ZipApi {

    @POST("/v1/charges")
    suspend fun captureZipCharge(
        @Header("X-Access-Token") accessToken: String,
        @Body request: CaptureZipChargeRequest,
    ): CaptureChargeResponse
}

