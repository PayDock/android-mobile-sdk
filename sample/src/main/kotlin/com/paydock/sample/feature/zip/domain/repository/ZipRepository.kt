package com.paydock.sample.feature.zip.domain.repository

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.zip.data.api.dto.CaptureZipChargeRequest

interface ZipRepository {
    suspend fun captureZipCharge(
        accessToken: String,
        request: CaptureZipChargeRequest,
    ): ChargeResponse
}

