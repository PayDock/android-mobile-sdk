package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage
import java.io.IOException

/**
 * A sealed class representing exceptions that can occur during the PayPal vault operations.
 *
 * This class extends [IOException] to provide a specific hierarchy of exceptions related
 * to PayPal vault interactions. Each exception includes a displayable message that can
 * be shown to the user, which provides context about the error.
 *
 * @param displayableMessage The message that can be displayed to the user for this exception.
 */
sealed class PayPalVaultException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error creating a setup token.
     *
     * This exception encapsulates the [ApiErrorResponse] received from the server
     * when attempting to create a setup token. It provides details about the error
     * that occurred, including a displayable message derived from the API error response.
     *
     * @property error The [ApiErrorResponse] received from the server, containing
     *                 detailed information about the error.
     * @constructor Creates a new `CreateSetupTokenException` with the given
     *              [ApiErrorResponse]. The exception's message is set to the
     *              `displayableMessage` from the provided [ApiErrorResponse].
     */
    data class CreateSetupTokenException(
        val error: ApiErrorResponse
    ) : PayPalVaultException(error.displayableMessage)

    /**
     * Exception thrown when there is an error retrieving the PayPal Client ID.
     *
     * This exception encapsulates an [ApiErrorResponse] which contains details about the specific error
     * encountered during the retrieval process.  It extends [PayPalVaultException] to indicate that
     * the error is related to the PayPal Vault operations.
     *
     * @property error The [ApiErrorResponse] object containing the details of the error. This includes
     *                 information like error codes, descriptions, and potentially more context.
     * @constructor Creates a new GetPayPalClientIdException with the provided [ApiErrorResponse].
     *              The exception message is derived from the `displayableMessage` property of the
     *              [ApiErrorResponse].
     */
    data class GetPayPalClientIdException(
        val error: ApiErrorResponse
    ) : PayPalVaultException(error.displayableMessage)

    /**
     * Exception thrown when there is a failure during the process of creating a payment token.
     *
     * This exception encapsulates the [ApiErrorResponse] received from the server,
     * providing details about the specific error that occurred.
     *
     * @property error The [ApiErrorResponse] received from the server, containing detailed error information.
     * @constructor Creates a [CreatePaymentTokenException] instance with the given [ApiErrorResponse].
     * @param error The [ApiErrorResponse] representing the error encountered during payment token creation.
     */
    data class CreatePaymentTokenException(
        val error: ApiErrorResponse
    ) : PayPalVaultException(error.displayableMessage)

    /**
     * Represents an exception specific to the PayPal SDK.
     *
     * This exception is thrown when an error occurs during an interaction with the PayPal SDK,
     * such as API calls, token management, or internal SDK operations. It provides a specific
     * error code and a human-readable description of the issue.
     *
     * @property code The specific error code associated with the exception. This code can be used
     *                  to identify the exact type of error that occurred.  Refer to PayPal's API
     *                  documentation for details on possible error codes.
     * @property description A human-readable description of the error that occurred. This provides
     *                       more context about the nature of the error and may assist in debugging.
     *                       This is the same message provided to the superclass `PayPalVaultException`.
     */
    data class PayPalSDKException(
        val code: Int,
        val description: String
    ) : PayPalVaultException(description) {

        /**
         * Returns a user-friendly error reason based on the error code.
         *
         * This provides more context about what went wrong, which can be helpful for logging,
         * analytics, or providing better error messages to end users.
         *
         * Common PayPal SDK error codes:
         * - 0: UNKNOWN - An unknown error occurred
         * - 1: DATA_PARSING_ERROR - Error parsing data from the server
         * - 2: UNKNOWN_HOST - Network connectivity issue or invalid host
         * - 3: NO_RESPONSE_DATA - Server returned no data
         * - 4: INVALID_URL_REQUEST - The URL request was malformed
         * - 5: SERVER_RESPONSE_ERROR - Server returned an error response
         * - 6: CHECKOUT_ERROR - Error during checkout flow
         * - 7: NATIVE_CHECKOUT_ERROR - Error in native checkout implementation
         * - 8: GRAPHQL_JSON_INVALID_ERROR - Invalid GraphQL JSON response
         *
         * @return A user-friendly string describing the error category
         */
        fun getErrorReason(): String = when (code) {
            0 -> "Unknown Error"
            1 -> "Data Parsing Error"
            2 -> "Network Connectivity Error"
            3 -> "No Response Data"
            4 -> "Invalid Request"
            5 -> "Server Error"
            6 -> "Checkout Error"
            7 -> "Native Checkout Error"
            8 -> "Invalid Response Format"
            else -> "PayPal SDK Error (Code: $code)"
        }

        /**
         * Returns the error category for grouping similar errors.
         *
         * This can be useful for analytics or error reporting to categorize
         * different types of errors for better tracking and debugging.
         *
         * @return The error category as an [ErrorCategory]
         */
        fun getErrorCategory(): ErrorCategory = when (code) {
            0 -> ErrorCategory.UNKNOWN
            1, 8 -> ErrorCategory.PARSING
            2 -> ErrorCategory.NETWORK
            3, 4, 5 -> ErrorCategory.SERVER
            6, 7 -> ErrorCategory.CHECKOUT
            else -> ErrorCategory.UNKNOWN
        }

        /**
         * Categories for PayPal SDK errors.
         */
        enum class ErrorCategory {
            /** Unknown or unclassified error */
            UNKNOWN,

            /** Error related to data parsing */
            PARSING,

            /** Error related to network connectivity */
            NETWORK,

            /** Error related to server communication */
            SERVER,

            /** Error related to checkout flow */
            CHECKOUT
        }
    }

    /**
     * Represents a cancellation exception during the PayPal vaulting process.
     *
     * This exception is thrown when a user or the system cancels the vaulting operation. It provides a
     * user-friendly message that can be displayed in the UI to indicate that the process was cancelled.
     *
     * @param displayableMessage The message that describes the reason for the cancellation, typically
     *        shown to the user.
     */
    class CancellationException(displayableMessage: String) : PayPalVaultException(displayableMessage)

    /**
     * Represents an exception that occurs during the parsing of data from an API call, typically JSON.
     *
     * This exception is thrown when there is an issue with the format or structure of
     * the data being parsed, preventing it from being processed correctly.
     *
     * @property displayableMessage A user-friendly message describing the parsing error.
     *                             This message is intended to be displayed to the user.
     * @property errorBody An optional string containing the JSON data that caused the parsing error.
     *                     This can be helpful for debugging purposes to pinpoint the exact
     *                     location and nature of the error within the data. If the error isn't related
     *                     to a particular JSON, it could be null.
     * @constructor Creates a new ParseException with the specified displayable message and
     *              optional error JSON.
     */
    class ParseException(displayableMessage: String, val errorBody: String?) : PayPalVaultException(displayableMessage)

    /**
     * Exception thrown when an unknown error occurs in PayPal vault operations.
     *
     * @param displayableMessage The message that can be displayed to the user for this exception.
     */
    class UnknownException(displayableMessage: String) : PayPalVaultException(displayableMessage)
}