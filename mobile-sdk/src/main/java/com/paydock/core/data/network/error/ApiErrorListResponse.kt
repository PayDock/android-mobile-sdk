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

import com.paydock.core.data.network.error.exceptions.ApiException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing an API error list response.
 *
 * @property status The HTTP status code.
 * @property error The API list error.
 * @property summary The error summary.
 */
@Serializable
internal data class ApiErrorListResponse(
    val status: Int,
    val error: ApiListError,
    @SerialName("error_summary") val summary: ErrorSummary
)

/**
 * Converts the [ApiErrorListResponse] to an [ApiException] with the specified [code].
 *
 * @param code The HTTP status code for the exception.
 * @return An [ApiException] instance with the error message from the response.
 */
internal fun ApiErrorListResponse.toApiError(code: Int) = ApiException(code, summary.message)

/**
 * Converts the [ApiErrorListResponse] to an [ApiException].
 *
 * @return An [ApiException] instance with the HTTP status code and error message from the response.
 */
internal fun ApiErrorListResponse.toApiError() = ApiException(status, summary.message)