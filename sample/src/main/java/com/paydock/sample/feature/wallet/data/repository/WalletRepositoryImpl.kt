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

package com.paydock.sample.feature.wallet.data.repository

import com.paydock.sample.feature.wallet.data.api.WalletApi
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.mapper.toDomain
import com.paydock.sample.feature.wallet.domain.model.WalletCharge
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val walletApi: WalletApi
) :
    WalletRepository {
    override suspend fun initiateWalletTransaction(manualCapture: Boolean, request: InitiateWalletRequest): WalletCharge =
        withContext(dispatcher) {
            if (manualCapture) {
                walletApi.initiateWalletTransactionManualCapture(request = request).toDomain()
            } else {
                walletApi.initiateWalletTransaction(request = request).toDomain()

            }
        }

    override suspend fun captureWalletCharge(chargeId: String): WalletCharge = withContext(dispatcher) {
        walletApi.captureWalletCharge(id = chargeId).toDomain()
    }
}