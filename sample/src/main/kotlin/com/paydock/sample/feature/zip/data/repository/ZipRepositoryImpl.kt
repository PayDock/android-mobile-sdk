package com.paydock.sample.feature.zip.data.repository

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.checkout.data.mapper.toDomain
import com.paydock.sample.feature.zip.data.api.ZipApi
import com.paydock.sample.feature.zip.data.api.dto.CaptureZipChargeRequest
import com.paydock.sample.feature.zip.domain.repository.ZipRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ZipRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val zipApi: ZipApi,
) : ZipRepository {

    override suspend fun captureZipCharge(
        accessToken: String,
        request: CaptureZipChargeRequest,
    ): ChargeResponse =
        withContext(dispatcher) {
            zipApi.captureZipCharge(accessToken = accessToken, request = request).toDomain()
        }
}

