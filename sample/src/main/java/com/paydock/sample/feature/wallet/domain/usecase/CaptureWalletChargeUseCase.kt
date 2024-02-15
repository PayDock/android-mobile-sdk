/*
 * Created by Paydock on 11/23/23, 11:56 AM
 * Copyright (c) 2023 Lasting. All rights reserved.
 *
 * Last modified 11/23/23, 11:56 AM
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

package com.paydock.sample.feature.wallet.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.repository.CardRepository
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class CaptureWalletChargeUseCase @Inject constructor(private val repository: WalletRepository) {

    suspend operator fun invoke(chargeId: String) =
        suspendRunCatching {
            repository.captureWalletCharge(chargeId)
        }
}