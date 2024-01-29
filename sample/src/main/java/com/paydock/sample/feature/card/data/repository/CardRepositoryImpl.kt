/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    override suspend fun tokeniseCardDetails(request: TokeniseCardRequest): String =
        withContext(dispatcher) {
            cardApi.tokeniseCardDetails(request = request).resource.resourceData
        }

    override suspend fun createCardVaultToken(request: VaultTokenRequest.CreateCardVaultTokenRequest): String =
        withContext(dispatcher) {
            cardApi.createVaultToken(request = request).resource.resourceData.token
        }

    override suspend fun createCardVaultToken(request: VaultTokenRequest.CreateCardSessionVaultTokenRequest): String =
        withContext(dispatcher) {
            cardApi.createVaultToken(request = request).resource.resourceData.token
        }

    override suspend fun captureCardCharge(request: CaptureCardChargeRequest): ChargeResponse =
        withContext(dispatcher) {
            cardApi.captureCharge(request = request)
        }
}