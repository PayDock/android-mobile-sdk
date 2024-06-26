package com.paydock.sample.feature.threeDS.data.repository

import com.paydock.sample.feature.threeDS.data.api.ThreeDSApi
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
    private val threeDSApi: ThreeDSApi
) : ThreeDSRepository {

    override suspend fun createIntegrated3dsToken(request: CreateIntegratedThreeDSTokenRequest): ThreeDSToken =
        withContext(dispatcher) {
            val response = threeDSApi.createIntegrated3dsToken(request = request)
            response.mapToDomain()
        }

    override suspend fun createStandalone3dsToken(request: CreateStandaloneThreeDSTokenRequest): ThreeDSToken =
        withContext(dispatcher) {
            threeDSApi.createStandalone3dsToken(request = request).mapToDomain()
        }
}