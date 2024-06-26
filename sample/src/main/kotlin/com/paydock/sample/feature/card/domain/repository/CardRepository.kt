package com.paydock.sample.feature.card.domain.repository

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest

interface CardRepository {
    suspend fun tokeniseCardDetails(request: TokeniseCardRequest): String
    suspend fun createCardVaultToken(request: VaultTokenRequest.CreateCardVaultTokenRequest): String
    suspend fun createCardVaultToken(request: VaultTokenRequest.CreateCardSessionVaultTokenRequest): String
    suspend fun captureCardCharge(request: CaptureCardChargeRequest): ChargeResponse
}