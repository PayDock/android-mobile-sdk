package com.paydock.core.data.network.error

import android.util.Log
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.ApiException
import com.paydock.core.domain.error.exceptions.UnknownApiException
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

            // Process the error data class accordingly and throw the appropriate ApiException
            throw when {
                apiErrorResponse != null -> apiErrorResponse.toApiError()
                else -> UnknownApiException(status = response.code, errorBody = errorBody)
            }
        } else {
            // Your existing error handling logic if deserialization fails
            val newErrorBody = errorBody?.toResponseBody("application/json".toMediaType())
            Log.e(
                MobileSDKConstants.MOBILE_SDK_TAG,
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
}
