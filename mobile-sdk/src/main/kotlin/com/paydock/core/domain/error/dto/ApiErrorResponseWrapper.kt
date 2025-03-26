package com.paydock.core.domain.error.dto

import com.paydock.core.network.dto.Resource
import com.paydock.core.network.dto.error.ApiError
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorDetails
import com.paydock.core.network.dto.error.ErrorMessage
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.ErrorSummaryDetails
import kotlinx.serialization.Serializable

/**
 * Represents an error response received from the SDK API.
 *
 * This class encapsulates the raw [ApiErrorResponse] and provides access to its
 * components through simplified properties, adapting them to the SDK's error handling model.
 *
 * @property response The underlying raw [ApiErrorResponse] from the API.
 * @constructor Creates an [SdkApiErrorResponse] instance with the provided [ApiErrorResponse].
 */
@Serializable
data class SdkApiErrorResponse(
    private val response: ApiErrorResponse
) {
    val status: Int
        get() = response.status

    val error: SdkApiError?
        get() = response.error?.let { SdkApiError(it) }

    val resource: SdkResource<Unit>?
        get() = response.resource?.let { SdkResource(it) }

    val summary: SdkErrorSummary
        get() = SdkErrorSummary(response.summary)
}

/**
 * Represents an error returned by the SDK API.
 *
 * This class encapsulates an underlying `ApiError` and exposes a user-friendly
 * interface to access error details. It simplifies the interaction with errors
 * received from the SDK API.
 *
 * The primary purpose of `SdkApiError` is to provide a more convenient way to
 * handle errors by extracting and presenting the most relevant information
 * from the raw `ApiError` in a structured manner.
 *
 * @property error The underlying [ApiError] object containing the raw error information.
 * @property message A human-readable error message describing the problem. This is extracted from the underlying `ApiError`.
 * @property code A string representing the error code. This is extracted from the underlying `ApiError`.
 * @property details Optional, detailed information about the error, if available. This is a wrapped `SdkErrorDetails` object,
 * which is generated from `ApiError` details. If the original `ApiError` has no details, this is null.
 */
@Serializable
data class SdkApiError(
    private val error: ApiError
) {
    val message: String
        get() = error.message

    val code: String
        get() = error.code

    val details: SdkErrorDetails?
        get() = error.details?.let { SdkErrorDetails(it) }
}

/**
 * Represents detailed information about an error encountered within the SDK.
 *
 * This class provides a structured and user-friendly way to access comprehensive error details
 * that might occur during interactions with the underlying service. It acts as a wrapper
 * around the internal `ErrorDetails` object, exposing key error information in a readily
 * understandable format.
 *
 * **Key Features:**
 *
 * *   **Structured Error Data:** Presents error details in a well-organized manner,
 *     making it easier to identify the cause and location of the issue.
 * *   **Gateway-Specific Information:** Includes fields for gateway-specific error codes
 *     and descriptions, which are crucial for troubleshooting integration-level problems.
 * *   **Parameter and Path Details:**  Provides information about the specific parameter
 *     or API path involved in the error, helping to pinpoint the source of the issue.
 * *   **User-Friendly Messages:**  Exposes error messages through the `messages` property,
 *     making it easier to convey the problem to the end user or to log for debugging.
 * * **Status Code Information:** Includes the status code and its description for accurate error identification.
 * *   **Wrapper for `ErrorDetails`:**  Encapsulates the raw `ErrorDetails` object, simplifying
 *     access to the underlying data.
 *
 * **Properties:**
 *
 * *   `gatewaySpecificCode`: An optional string representing a code specific to the gateway
 *      where the error originated.
 * *   `gatewaySpecificDescription`: An optional string providing a description of the error
 *      from the gateway's perspective.
 * *   `paramName`: An optional string indicating the name of the parameter associated with the
 *      error, if applicable.
 * *   `description`: An optional string providing a general description of the error.
 * *   `path`: An optional string indicating the API path where the error occurred.
 * *   `messages`: An optional list of [SdkErrorMessage] objects, each containing a detailed
 *      error message.
 * *   `statusCode`: An optional string representing the HTTP status code associated with the error.
 * *  `statusCodeDescription`: An optional string providing the description of the http status code.
 *
 * @constructor Creates an `SdkErrorDetails` instance.
 * @param errorDetails The underlying [ErrorDetails] object containing */
@Serializable
class SdkErrorDetails(
    private val errorDetails: ErrorDetails
) {
    val gatewaySpecificCode: String?
        get() = errorDetails.gatewaySpecificCode

    val gatewaySpecificDescription: String?
        get() = errorDetails.gatewaySpecificDescription

    val paramName: String?
        get() = errorDetails.paramName

    val description: String?
        get() = errorDetails.description

    val path: String?
        get() = errorDetails.path

    val messages: List<SdkErrorMessage>?
        get() = errorDetails.messages?.map {
            when (it) {
                is ErrorMessage.StringMessage -> SdkErrorMessage.StringMessage(it)
                is ErrorMessage.GatewayError -> SdkErrorMessage.GatewayError(it)
            }
        }

    val statusCode: String?
        get() = errorDetails.statusCode

    val statusCodeDescription: String?
        get() = errorDetails.statusCodeDescription
}

/**
 * SdkResource is a wrapper class around a [Resource] object, providing a standardized
 * way to represent data retrieved from an SDK. It encapsulates the underlying
 * [Resource] and exposes its type and data in a more accessible manner.
 *
 * This class is serializable using kotlinx.serialization, allowing it to be easily
 * converted to and from JSON or other supported formats.
 *
 * @param T The type of the data held within the resource.
 * @property resource The underlying [Resource] object containing the data and its type.
 */
@Serializable
data class SdkResource<T>(
    private val resource: Resource<T>
) {
    val type: String
        get() = resource.type

    val data: T?
        get() = resource.data
}

/**
 * Represents a summary of an error that occurred within the SDK.
 *
 * This class provides a high-level overview of an error, including a message, code,
 * status code (if applicable), status code description (if applicable), and optional
 * detailed information. It wraps an internal [ErrorSummary] object, exposing
 * relevant fields in a more user-friendly manner for SDK consumers.
 *
 * @property summary The underlying [ErrorSummary] object containing the raw error data.
 */
@Serializable
data class SdkErrorSummary(
    private val summary: ErrorSummary
) {
    val message: String
        get() = summary.message

    val code: String
        get() = summary.code

    val statusCode: String?
        get() = summary.statusCode

    val statusCodeDescription: String?
        get() = summary.statusCodeDescription

    val details: SdkErrorSummaryDetails?
        get() = summary.details?.let { SdkErrorSummaryDetails(it) }
}

/**
 * Represents a summary of details related to an SDK error.
 *
 * This class encapsulates the `ErrorSummaryDetails` and provides
 * convenient access to its properties like gateway-specific code,
 * description, messages, status code, etc. It acts as a wrapper,
 * exposing the error details in a more structured and easily consumable
 * format specifically tailored for SDK error reporting.
 *
 * @property gatewaySpecificCode A gateway-specific code associated with the error, if available.
 *           This can be null if no such code is provided by the gateway.
 * @property gatewaySpecificDescription A gateway-specific description of the error, if available.
 *           This can be null if no such description is provided by the gateway.
 * @property messages A list of error messages related to the error.
 *           This can be null or empty if no specific messages are provided.
 * @property description A general description of the error.
 *           This can be null if no description is provided.
 * @property statusCode The HTTP status code associated with the error, if applicable.
 *           This can be null if the error is not related to an HTTP request.
 * @property statusCodeDescription A description of the HTTP status code, if applicable.
 *           This can be null if no status code description is provided.
 * @constructor Creates an `SdkErrorSummaryDetails` instance.
 * @param summaryDetails The underlying `ErrorSummaryDetails` instance containing the raw error information.
 */
@Serializable
data class SdkErrorSummaryDetails(
    private val summaryDetails: ErrorSummaryDetails
) {
    val gatewaySpecificCode: String?
        get() = summaryDetails.gatewaySpecificCode

    val gatewaySpecificDescription: String?
        get() = summaryDetails.gatewaySpecificDescription

    val messages: List<String>?
        get() = summaryDetails.messages

    val description: String?
        get() = summaryDetails.description

    val statusCode: String?
        get() = summaryDetails.statusCode

    val statusCodeDescription: String?
        get() = summaryDetails.statusCodeDescription

}

/**
 * Represents an error message returned by the SDK.
 *
 * This sealed class encapsulates different types of error messages that can be returned by the SDK.
 * It provides a structured way to handle various error scenarios, including simple string messages
 * and more complex gateway-specific errors.
 */
@Serializable
sealed class SdkErrorMessage {
    /**
     * Represents an error message that is a simple string.
     *
     * This class wraps an `ErrorMessage.StringMessage` to provide a more convenient
     * way to access the string message. It implements `SdkErrorMessage` to be used in a broader
     * error handling context within the SDK.
     *
     * @property errorMessage The underlying `ErrorMessage.StringMessage` containing the string.
     * @property message The actual string message extracted from `errorMessage`.
     */
    data class StringMessage(private val errorMessage: ErrorMessage.StringMessage) :
        SdkErrorMessage() {
        val message: String
            get() = errorMessage.message
    }

    /**
     * Represents an error that occurred during communication with a gateway.
     *
     * This class encapsulates detailed information about a gateway-specific error,
     * including error codes, descriptions, and status codes. It extends `SdkErrorMessage`
     * to provide a consistent way of handling errors within the SDK.
     *
     * @property errorMessage The underlying [ErrorMessage.GatewayError] object containing the error details.
     * It's `private` to enforce access via the exposed properties.
     *
     * @property gatewaySpecificCode The specific error code returned by the gateway.
     *             This can be used to identify the particular type of error encountered.
     *             It can be null if no gateway-specific code was provided.
     *
     * @property gatewaySpecificDescription A human-readable description of the gateway-specific error.
     *             This provides context about the error in the gateway's own terminology.
     *             It can be null if no gateway-specific description was provided.
     *
     * @property description A general description of the error.
     *           This is a more generic description that may be shared across different error types.
     *           It can be null if no description was provided.
     *
     * @property statusCode The HTTP status code associated with the error.
     *           This usually indicates the general nature of the error (e.g., 400 Bad Request, 500 Internal Server Error).
     *           It can be null if no status code was provided.
     *
     * @property statusCodeDescription A human-readable description of the HTTP status code.
     *           This provides more context about the meaning of the status code.
     *           It can be null if no status code description was provided.
     */
    data class GatewayError(
        private val errorMessage: ErrorMessage.GatewayError
    ) : SdkErrorMessage() {
        val gatewaySpecificCode: String?
            get() = errorMessage.gatewaySpecificCode
        val gatewaySpecificDescription: String?
            get() = errorMessage.gatewaySpecificDescription
        val description: String?
            get() = errorMessage.description
        val statusCode: String?
            get() = errorMessage.statusCode
        val statusCodeDescription: String?
            get() = errorMessage.statusCodeDescription
    }
}

/**
 * A user-friendly message that can be displayed to the user to explain the error.
 *
 * This property extracts the `message` from the `summary` property of the `SdkApiErrorResponse` if it exists.
 * If the `SdkApiErrorResponse` or its `summary` is null, an empty string is returned.
 *
 * This ensures a consistent way to access a displayable error message, handling cases where the message
 * might not be present.
 *
 * @return The error message from the response's summary, or an empty string if the response or summary is null.
 */
val SdkApiErrorResponse?.displayableMessage: String
    get() = this?.summary?.message ?: ""