/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
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

package com.paydock.core.data.network.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing details of an error summary.
 *
 * @property gatewaySpecificCode Specific code related to the gateway (nullable).
 * @property gatewaySpecificDescription Specific description related to the gateway (nullable).
 * @property messages List of additional messages related to the error (nullable list).
 * @property description Description of the error (nullable).
 * @property statusCode Status code related to the error (nullable).
 * @property statusCodeDescription Description of the status code (nullable).
 */
@Serializable
data class ErrorSummaryDetails(
    @SerialName("gateway_specific_code") val gatewaySpecificCode: String? = null,
    @SerialName("gateway_specific_description") val gatewaySpecificDescription: String? = null,
    val messages: List<String>? = null,
    val description: String? = null,
    @SerialName("status_code") val statusCode: String? = null,
    @SerialName("status_code_description") val statusCodeDescription: String? = null
)