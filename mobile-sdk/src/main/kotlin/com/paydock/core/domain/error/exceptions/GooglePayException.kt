package com.paydock.core.domain.error.exceptions

import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage
import java.io.IOException

/**
 * Exception thrown when there's an error related to Google Pay integration.
 *
 * @constructor Creates a GooglePayException with the specified displayable message.
 */
@Suppress("MaxLineLength")
sealed class GooglePayException(displayableMessage: String) : IOException(displayableMessage) {

    /**
     * Exception thrown when there is an error capturing the charge for Google Pay.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a CapturingChargeException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class CapturingChargeException(
        val error: ApiErrorResponse
    ) : GooglePayException(error.displayableMessage)

    /**
     * Exception thrown when there is an error during the payment request process for Google Pay.
     *
     * At this stage, the user has already seen a popup informing them an error occurred.
     * Normally, only logging is required.
     *
     * @property exception The underlying exception causing this error.
     * @see WalletConstants Library [Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     * @constructor Creates a PaymentRequestException with the specified underlying exception.
     *              The displayable message is derived from the exception's message or a default Google Pay error message.
     */
    class PaymentRequestException(val exception: Exception?) :
        GooglePayException(exception?.message ?: MobileSDKConstants.Errors.GOOGLE_PAY_ERROR)

    /**
     * Exception thrown when there is an initialization error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an InitialisationException with the specified displayable message.
     */
    class InitialisationException(displayableMessage: String) :
        GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is a result error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a ResultException with the specified displayable message.
     */
    class ResultException(displayableMessage: String) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(displayableMessage: String) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : GooglePayException(displayableMessage)
}
