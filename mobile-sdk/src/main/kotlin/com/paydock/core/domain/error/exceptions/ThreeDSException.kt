package com.paydock.core.domain.error.exceptions

import java.io.IOException

/**
 * Represents an exception related to 3D Secure operations.
 *
 * @constructor Creates a ThreeDSException with the specified displayable message.
 */
sealed class ThreeDSException(displayableMessage: String) : IOException(displayableMessage) {

    /**
     * Exception thrown when there is an error during the charge process for 3D Secure.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a ChargeErrorException with the specified displayable message.
     */
    class ChargeErrorException(displayableMessage: String) : ThreeDSException(displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @param code The HTTP code of the response, if available.
     * @param displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        ThreeDSException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to 3D Secure.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(displayableMessage: String) : ThreeDSException(displayableMessage)
}
