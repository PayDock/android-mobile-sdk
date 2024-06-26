package com.paydock.core.domain.error.exceptions

import java.io.IOException

/**
 * Represents an exception related to Mastercard SRC operations.
 *
 * @constructor Creates a MastercardSRCException with the specified displayable message.
 */
sealed class MastercardSRCException(displayableMessage: String) : IOException(displayableMessage) {

    /**
     * Exception thrown when there is an error during the checkout process for Mastercard SRC.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CheckoutErrorException with the specified displayable message.
     */
    class CheckoutErrorException(displayableMessage: String) :
        MastercardSRCException(displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @param code The HTTP code of the response, if available.
     * @param displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        MastercardSRCException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to Mastercard SRC.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(displayableMessage: String) :
        MastercardSRCException(displayableMessage)
}
