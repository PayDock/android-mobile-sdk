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

package com.paydock.feature.wallet.domain.repository

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * A repository interface responsible for capturing wallet transactions.
 */
internal interface WalletRepository {

    /**
     * Capture a wallet transaction with the provided token and request details.
     *
     * @param token The authentication token required for the transaction.
     * @param request The request object containing transaction details.
     * @return A [WalletCaptureResponse] representing the result of the transaction capture.
     */
    suspend fun captureWalletTransaction(token: String, request: WalletCaptureRequest): ChargeResponse

    /**
     * Retrieve wallet callback information from the server.
     *
     * @param token The authentication token for accessing the wallet callback information.
     * @param request The request object specifying the details of the wallet callback request.
     *
     * @return A callback details representing the wallet callback information retrieved from the server.
     */
    suspend fun getWalletCallback(token: String, request: WalletCallbackRequest): WalletCallback?

}
