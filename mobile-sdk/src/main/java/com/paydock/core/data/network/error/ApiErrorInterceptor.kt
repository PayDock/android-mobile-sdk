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

package com.paydock.core.data.network.error

import android.util.Log
import com.paydock.core.data.network.error.exceptions.ApiException
import com.paydock.core.extensions.convertToDataClass
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Intercepts API errors and converts them into [ApiException] by decoding the Error response body.
 * In case serialization fails, the response will be delivered as is.
 */
class ApiErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Check if the response is successful
        if (response.isSuccessful) {
            return response
        }

        // Extract error body from the response
        val errorBody = response.body?.string()

        // If the error body is present, attempt to convert to specific error response structures
        if (!errorBody.isNullOrBlank()) {
            // Depending on the error structure, convert to appropriate data class
            val apiErrorResponse = convertToApiErrorResponse(errorBody)
            val apiErrorListResponse = convertToApiListErrorResponse(errorBody)
            val apiErrorMessageListResponse = convertToApiMessageListErrorResponse(errorBody)

            // Process the error data class accordingly and throw the appropriate ApiException
            throw when {
                apiErrorResponse != null -> apiErrorResponse.toApiError()
                apiErrorListResponse != null -> apiErrorListResponse.toApiError()
                apiErrorMessageListResponse != null -> apiErrorMessageListResponse.toApiError()
                else -> ApiException(response.code, errorBody)
            }
        } else {
            // Your existing error handling logic if deserialization fails
            val newErrorBody = errorBody?.toResponseBody("application/json".toMediaType())
            Log.e(
                ApiErrorInterceptor::class.simpleName,
                "Failed to deserialize error body: $newErrorBody"
            )
            return response.newBuilder().body(newErrorBody).build()
        }
    }

    /**
     * Attempts to convert the error response to an [ApiErrorResponse].
     */
    private fun convertToApiErrorResponse(errorResponse: String): ApiErrorResponse? =
        runCatching<ApiErrorResponse> {
            errorResponse.convertToDataClass()
        }.getOrNull()

    /**
     * Attempts to convert the error response to an [ApiErrorListResponse].
     */
    private fun convertToApiListErrorResponse(errorResponse: String): ApiErrorListResponse? =
        runCatching<ApiErrorListResponse> {
            errorResponse.convertToDataClass()
        }.getOrNull()

    /**
     * Attempts to convert the error response to an [ApiErrorMessagesListResponse].
     */
    private fun convertToApiMessageListErrorResponse(errorResponse: String): ApiErrorMessagesListResponse? =
        runCatching<ApiErrorMessagesListResponse> {
            errorResponse.convertToDataClass()
        }.getOrNull()
}
