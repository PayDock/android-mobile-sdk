package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Represents an exception related to Coles Pay operations.
 *
 * @constructor Creates a ColesPayException with the specified displayable message.
 */
sealed class ColesPayException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error fetching the URL for Coles Pay.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a FetchingUrlException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class FetchingUrlException(
        val error: ApiErrorResponse
    ) : ColesPayException(error.displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @property code The HTTP code of the response, if available.
     * @property displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        ColesPayException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to Coles Pay.
     *
     * @property displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(displayableMessage: String) : ColesPayException(displayableMessage)

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
    class ParseException(displayableMessage: String, val errorBody: String?) : ColesPayException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Coles Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : ColesPayException(displayableMessage)
}
