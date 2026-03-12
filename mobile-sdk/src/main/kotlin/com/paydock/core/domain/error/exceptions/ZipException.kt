package com.paydock.core.domain.error.exceptions

import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Represents an exception related to Zip payment operations.
 *
 * @constructor Creates a ZipException with the specified displayable message.
 */
sealed class ZipException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error fetching the Zip checkout URL.
     *
     * @property error The underlying API error response causing this exception.
     * @constructor Creates a FetchingCheckoutUrlException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class FetchingCheckoutUrlException(
        val error: ApiErrorResponse
    ) : ZipException(error.displayableMessage)

    /**
     * Exception thrown when there is an error creating the payment source token.
     *
     * @property error The underlying API error response causing this exception.
     * @constructor Creates a CreatingPaymentSourceTokenException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class CreatingPaymentSourceTokenException(
        val error: ApiErrorResponse
    ) : ZipException(error.displayableMessage)

    /**
     * Exception thrown when there is an error while communicating with the WebView.
     *
     * @property code The HTTP or WebView error code, if available.
     * @property displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        ZipException(displayableMessage)

    /**
     * Exception thrown when the Zip transaction was cancelled by the user.
     *
     * @property checkoutId The Zip checkout ID, if available.
     * @constructor Creates a CancellationException with the specified checkout ID.
     */
    class CancellationException(
        val checkoutId: String? = null,
        displayableMessage: String = MobileSDKConstants.ZipConfig.Errors.CANCELLATION_ERROR
    ) : ZipException(displayableMessage)

    /**
     * Exception thrown when the Zip transaction was declined.
     *
     * @property checkoutId The Zip checkout ID, if available.
     * @constructor Creates a DeclinedException with the specified checkout ID.
     */
    class DeclinedException(
        val checkoutId: String? = null,
        displayableMessage: String = MobileSDKConstants.ZipConfig.Errors.DECLINED_ERROR
    ) : ZipException(displayableMessage)

    /**
     * Exception thrown when the Zip transaction requires manual review (referred).
     *
     * @property checkoutId The Zip checkout ID, if available.
     * @constructor Creates a ReferredException with the specified checkout ID.
     */
    class ReferredException(
        val checkoutId: String? = null,
        displayableMessage: String = MobileSDKConstants.ZipConfig.Errors.REFERRED_ERROR
    ) : ZipException(displayableMessage)

    /**
     * Exception thrown when an unexpected status is received from Zip.
     *
     * @property status The unexpected status string received.
     * @property checkoutId The Zip checkout ID, if available.
     * @constructor Creates an UnexpectedStatusException with the status and checkout ID.
     */
    class UnexpectedStatusException(
        val status: String?,
        val checkoutId: String? = null,
        displayableMessage: String = "Unexpected Zip status: ${status ?: "unknown"}"
    ) : ZipException(displayableMessage)

    /**
     * Exception thrown when there is an error parsing an API response for Zip operations.
     *
     * @property errorBody The raw error body from the API response.
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a ParseException with the specified displayable message and error body.
     */
    class ParseException(
        displayableMessage: String,
        val errorBody: String? = null
    ) : ZipException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Zip.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(
        displayableMessage: String = MobileSDKConstants.ZipConfig.Errors.UNKNOWN_ERROR
    ) : ZipException(displayableMessage)
}
