package com.paydock.core.domain.error.exceptions

/**
 * Represents an exception related to ClickToPay operations.
 *
 * @constructor Creates a ClickToPayException with the specified displayable message.
 */
sealed class ClickToPayException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error during the checkout process for ClickToPay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CheckoutErrorException with the specified displayable message.
     */
    class CheckoutErrorException(displayableMessage: String) :
        ClickToPayException(displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @param code The HTTP code of the response, if available.
     * @param displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        ClickToPayException(displayableMessage)
}
