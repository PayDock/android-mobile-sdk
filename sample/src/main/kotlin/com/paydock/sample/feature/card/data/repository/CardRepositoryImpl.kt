package com.paydock.sample.feature.card.data.repository

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.sample.feature.card.data.api.CardApi
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.repository.CardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val cardApi: CardApi
) : CardRepository {

    override suspend fun tokeniseCardDetails(
        accessToken: String,
        request: TokeniseCardRequest
    ): String =
        withContext(dispatcher) {
            cardApi.tokeniseCardDetails(
                accessToken = accessToken,
                request = request
            ).resource.resourceData
        }

    override suspend fun createCardVaultToken(request: VaultTokenRequest): String =
        withContext(dispatcher) {
            cardApi.createVaultToken(request = request).resource.resourceData.token
        }

    override suspend fun captureCardCharge(request: CaptureCardChargeRequest): ChargeResponse =
        withContext(dispatcher) {
            cardApi.captureCharge(request = request)
        }
}