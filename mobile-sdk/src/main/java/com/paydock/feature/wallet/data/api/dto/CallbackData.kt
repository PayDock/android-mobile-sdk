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
 * Represents the wallet callback data.
 *
 * @property id The unique identifier for the wallet callback.
 * @property status The status of the wallet callback.
 * @property callbackMethod The callback method used for the wallet callback.
 * @property callbackRel The callback relation.
 * @property callbackUrl The URL to which the callback is made.
 * @property charge Information about the associated charge.
 */
@Serializable
data class CallbackData(
    val id: String,
    val status: String? = null,
    @SerialName("callback_method") val callbackMethod: String? = null,
    @SerialName("callback_rel") val callbackRel: String? = null,
    @SerialName("callback_url") val callbackUrl: String? = null,
    val charge: CallbackCharge
)