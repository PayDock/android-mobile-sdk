/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
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

package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.repository.WalletRepository

/**
 * Use case for fetching wallet callback information.
 *
 * @param repository The repository responsible for interacting with wallet callback data.
 */
internal class GetWalletCallbackUseCase(private val repository: WalletRepository) {
    /**
     * Invoke the use case to fetch wallet callback data.
     *
     * @param token The authentication token for accessing wallet callback data.
     * @param request The request object specifying the details of the wallet callback data to fetch.
     *
     * @return A [Result] representing the outcome of the wallet callback data retrieval.
     */
    suspend operator fun invoke(token: String, request: WalletCallbackRequest): Result<WalletCallback?> =
        suspendRunCatching {
            repository.getWalletCallback(token, request)
        }
}
