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

package com.paydock.core.domain.error

import com.google.android.gms.common.api.ResolvableApiException
import com.paydock.core.data.network.error.exceptions.ApiException
import com.paydock.core.data.network.error.exceptions.ComponentException
import com.paydock.core.data.network.error.exceptions.GooglePayException
import com.paydock.core.data.network.error.exceptions.WebViewException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Represents various types of errors that can occur within the application.
 */
sealed interface ErrorModel {

    /**
     * API Error: Indicates an error related to the API request or response.
     *
     * @property exception The [ApiException] instance representing the API error.
     */
    data class ApiError(val exception: ApiException) : ErrorModel

    /**
     * Connection Error: Represents different connection-related issues.
     */
    sealed class ConnectionError : ErrorModel {

        /** Timeout error for connection. */
        data object Timeout : ConnectionError()

        /** Input/Output error for connection. */
        data object IOError : ConnectionError()

        /** Unknown host error for connection. */
        data object UnknownHost : ConnectionError()
    }

    /**
     * Serialization Error: Indicates an error during serialization process.
     *
     * @property throwable The [Throwable] representing the serialization error.
     */
    data class SerializationError(val throwable: Throwable) : ErrorModel

    /**
     * Unknown Error: Represents an unknown error encountered.
     *
     * @property throwable The [Throwable] representing the unknown error.
     */
    data class UnknownError(val throwable: Throwable) : ErrorModel

    /**
     * Google Pay Error: Represents errors specific to Google Pay functionality.
     *
     * @property exception The [Exception] specific to Google Pay.
     */
    data class GooglePayError(val exception: Exception) : ErrorModel

    /**
     * Error class representing an error caused or thrown from a composable component.
     *
     * @property exception The [IOException] associated with the error.
     */
    data class ComponentError(val exception: IOException) : ErrorModel

    /**
     * Error class representing a PayPal-specific error caused or thrown from a composable component.
     *
     * @property exception The [IOException] associated with the error.
     */
    data class PayPalError(val exception: IOException) : ErrorModel

    /**
     * Error class representing a FlyPay-specific error caused or thrown from a composable component.
     *
     * @property exception The [IOException] associated with the error.
     */
    data class FlyPayError(val exception: IOException) : ErrorModel

    /**
     * Error class representing a WebView-specific error caused or thrown from a composable component.
     *
     * @property exception The [WebViewException] associated with the error.
     */
    data class WebViewError(val exception: WebViewException) : ErrorModel
}

/**
 * Converts various types of [Throwable] instances to corresponding [ErrorModel] instances.
 * @return The appropriate [ErrorModel] instance based on the given Throwable.
 */
@Suppress("MaxLineLength")
fun Throwable.toError(): ErrorModel {
    return when (this) {
        is ApiException -> ErrorModel.ApiError(this)
        is ComponentException -> ErrorModel.ComponentError(this)
        // WebView Errors
        is WebViewException.PayPalException -> ErrorModel.PayPalError(this)
        is WebViewException.FlyPayException -> ErrorModel.FlyPayError(this)
        is WebViewException.UnknownException -> ErrorModel.WebViewError(this)
        // Google Pay Exceptions
        is ResolvableApiException -> ErrorModel.GooglePayError(this)
        /**
         * At this stage, the user has already seen a popup informing them an error occurred. Normally,
         * only logging is required.
         *
         * statusCode will hold the value of any constant from CommonStatusCode or one of the
         * WalletConstants.ERROR_CODE_* constants.
         * @see [
         * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
         */
        is com.google.android.gms.common.api.ApiException -> ErrorModel.GooglePayError(this)
        is GooglePayException -> ErrorModel.GooglePayError(this)
        // Generic
        is SocketTimeoutException -> ErrorModel.ConnectionError.Timeout
        is UnknownHostException -> ErrorModel.ConnectionError.UnknownHost
        is IOException -> ErrorModel.ConnectionError.IOError
        is SerializationException -> ErrorModel.SerializationError(this)
        else -> ErrorModel.UnknownError(this)
    }
}