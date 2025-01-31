package com.paydock.sample.feature.threeDS.data.repository

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.checkout.data.mapper.toDomain
import com.paydock.sample.feature.threeDS.data.api.ThreeDSApi
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
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
    override suspend fun createIntegrated3dsToken(
        accessToken: String,
        request: CreateIntegratedThreeDSTokenRequest
    ): ThreeDSToken =
        withContext(dispatcher) {
            threeDSApi.createIntegrated3dsToken(accessToken = accessToken, request = request).mapToDomain()
        }

    override suspend fun createStandalone3dsToken(request: CreateStandaloneThreeDSTokenRequest): ThreeDSToken =
        withContext(dispatcher) {
            threeDSApi.createStandalone3dsToken(request = request).mapToDomain()
        }

    override suspend fun capture3DSCharge(request: Capture3DSChargeRequest): ChargeResponse =
        withContext(dispatcher) {
            threeDSApi.capture3DSCharge(request = request).toDomain()
        }
}