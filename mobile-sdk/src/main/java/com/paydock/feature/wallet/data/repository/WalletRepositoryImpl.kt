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

package com.paydock.feature.wallet.data.repository

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.repository.WalletRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Implementation of the [WalletRepository] interface responsible for capturing wallet transactions.
 *
 * @param dispatcher The [CoroutineDispatcher] used to control the execution context.
 * @param client The [HttpClient] for making HTTP requests.
 */
internal class WalletRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : WalletRepository {

    /**
     * Capture a wallet transaction with the provided token and request details.
     *
     * @param token The authentication token required for the transaction.
     * @param request The request object containing transaction details.
     * @return A [WalletCaptureResponse] representing the result of the transaction capture.
     */
    override suspend fun captureWalletTransaction(token: String, request: WalletCaptureRequest): ChargeResponse =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers {
                    append("x-access-token", token)
                    append("Content-Type", "application/json;charset=utf-8")
                }
                url { path("/v1/charges/wallet/capture") }
                setBody(request)
            }
            httpResponse.body<WalletCaptureResponse>().asEntity()
        }

    /**
     * Retrieve wallet callback information from the server.
     *
     * @param token The authentication token for accessing the wallet callback information.
     * @param request The request object specifying the details of the wallet callback request.
     *
     * @return A callback url representing the wallet callback information retrieved from the server.
     */
    override suspend fun getWalletCallback(token: String, request: WalletCallbackRequest): WalletCallback =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers {
                    append("x-access-token", token)
                    append("Content-Type", "application/json;charset=utf-8")
                }
                url { path("/v1/charges/wallet/callback") }
                setBody(request)
            }
            httpResponse.body<WalletCallbackResponse>().asEntity()
        }

}
