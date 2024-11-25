package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Represents an exception related to FlyPay operations.
 *
 * @constructor Creates a FlyPayException with the specified displayable message.
 */
sealed class FlyPayException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error fetching the URL for FlyPay.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a FetchingUrlException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class FetchingUrlException(
        val error: ApiErrorResponse
    ) : FlyPayException(error.displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @property code The HTTP code of the response, if available.
     * @property displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        FlyPayException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to FlyPay.
     *
     * @property displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(displayableMessage: String) : FlyPayException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to FlyPay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : FlyPayException(displayableMessage)
}
