package com.paydock.core.domain.error.exceptions

import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.displayableMessage
import java.io.IOException

/**
 * Represents an exception related to PayPal operations.
 *
 * @property displayableMessage A human-readable message describing the error.
 * @constructor Creates a PayPalException with the specified displayable message.
 */
sealed class PayPalException(displayableMessage: String) : IOException(displayableMessage) {

    /**
     * Exception thrown when there is an error fetching the URL for PayPal.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a FetchingUrlException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class FetchingUrlException(
        val error: ApiErrorResponse
    ) : PayPalException(error.displayableMessage)

    /**
     * Exception thrown when there is an error capturing the charge for PayPal.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a CapturingChargeException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class CapturingChargeException(
        val error: ApiErrorResponse
    ) : PayPalException(error.displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @param code The HTTP code of the response, if available.
     * @param displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        PayPalException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to PayPal.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(displayableMessage: String) : PayPalException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to PayPal.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : PayPalException(displayableMessage)
}
