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

package com.paydock.sample.feature.wallet.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class InitiateWalletTransactionUseCase @Inject constructor(private val repository: WalletRepository) {

    suspend operator fun invoke(request: InitiateWalletRequest) = suspendRunCatching {
        repository.initiateWalletTransaction(request)
    }
}