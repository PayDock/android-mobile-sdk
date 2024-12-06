package com.paydock.sample.feature.charges.data.repository

import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.charges.data.api.ChargesApi
import com.paydock.sample.feature.charges.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.charges.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.charges.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.charges.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.charges.data.mapper.mapToDomain
import com.paydock.sample.feature.charges.data.mapper.toDomain
import com.paydock.sample.feature.charges.domain.model.ThreeDSToken
import com.paydock.sample.feature.charges.domain.model.WalletCharge
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChargesRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val chargesApi: ChargesApi,
) : ChargesRepository {

    override suspend fun captureCardCharge(request: CaptureCardChargeRequest): ChargeResponse =
        withContext(dispatcher) {
            chargesApi.captureCharge(request = request).toDomain()
        }

    override suspend fun initiateWalletTransaction(
        manualCapture: Boolean,
        request: InitiateWalletRequest,
    ): WalletCharge =
        withContext(dispatcher) {
            if (manualCapture) {
                chargesApi.initiateWalletTransactionManualCapture(request = request).toDomain()
            } else {
                chargesApi.initiateWalletTransaction(request = request).toDomain()

            }
        }

    override suspend fun captureWalletCharge(chargeId: String): WalletCharge =
        withContext(dispatcher) {
            chargesApi.captureWalletCharge(id = chargeId).toDomain()
        }

    override suspend fun createIntegrated3dsToken(
        accessToken: String,
        request: CreateIntegratedThreeDSTokenRequest,
    ): ThreeDSToken =
        withContext(dispatcher) {
            val response =
                chargesApi.createIntegrated3dsToken(accessToken = accessToken, request = request)
            response.mapToDomain()
        }

    override suspend fun createStandalone3dsToken(request: CreateStandaloneThreeDSTokenRequest): ThreeDSToken =
        withContext(dispatcher) {
            chargesApi.createStandalone3dsToken(request = request).mapToDomain()
        }
}