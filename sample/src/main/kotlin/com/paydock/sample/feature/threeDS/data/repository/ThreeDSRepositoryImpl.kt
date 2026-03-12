package com.paydock.sample.feature.threeDS.data.repository

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.checkout.data.mapper.toDomain
import com.paydock.sample.feature.threeDS.data.api.ThreeDSApi
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateMPGS3dsTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.mapper.mapToDomain
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken
import com.paydock.sample.feature.threeDS.domain.repository.ThreeDSRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ThreeDSRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val threeDSApi: ThreeDSApi,
) : ThreeDSRepository {
    override suspend fun createMPGS3dsToken(
        accessToken: String,
        request: CreateMPGS3dsTokenRequest
    ): ThreeDSToken =
        withContext(dispatcher) {
            threeDSApi.createMPGS3dsToken(accessToken = accessToken, request = request)
                .mapToDomain()
        }

    override suspend fun createStandalone3dsToken(
        accessToken: String,
        request: CreateStandaloneThreeDSTokenRequest
    ): ThreeDSToken =
        withContext(dispatcher) {
            threeDSApi.createStandalone3dsToken(accessToken = accessToken, request = request)
                .mapToDomain()
        }

    override suspend fun capture3DSCharge(
        accessToken: String,
        request: Capture3DSChargeRequest
    ): ChargeResponse =
        withContext(dispatcher) {
            threeDSApi.capture3DSCharge(accessToken = accessToken, request = request).toDomain()
        }
}