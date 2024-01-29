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

package com.paydock.feature.wallet.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a request object used to initiate a wallet callback.
 *
 * @property type The type of the wallet callback request, default is "CREATE_TRANSACTION".
 * @property shipping An optional flag indicating whether shipping information is included in the request.
 * @property sessionId An optional session identifier.
 * @property walletType An optional wallet type.
 */
@Serializable
internal data class WalletCallbackRequest(
    @SerialName("request_type") val type: String,
    @SerialName("request_shipping") val shipping: Boolean? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("wallet_type") val walletType: String? = null
)
