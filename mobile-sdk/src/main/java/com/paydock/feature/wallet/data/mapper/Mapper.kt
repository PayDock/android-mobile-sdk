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

package com.paydock.feature.wallet.data.mapper

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Converts a [WalletCaptureResponse] into a [ChargeResponse].
 *
 * @return A [ChargeResponse] entity representing the wallet capture response.
 */
internal fun WalletCaptureResponse.asEntity() = ChargeResponse(
    status = status,
    resource = ChargeResponse.ChargeResource(
        type = resource.type,
        data = resource.data?.let { data ->
            ChargeResponse.ChargeData(
                status = data.status,
                id = data.id,
                amount = data.amount,
                currency = data.currency
            )
        }
    )
)

/**
 * Extracts the callback URL from a [WalletCallbackResponse].
 *
 * @return The callback URL from the wallet callback response.
 */
internal fun WalletCallbackResponse.asEntity() = WalletCallback(
    callbackId = resource.data?.id,
    status = resource.data?.charge?.status,
    callbackUrl = resource.data?.callbackUrl
)