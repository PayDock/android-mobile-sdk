package com.paydock.core.domain.error.exceptions

import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Exception thrown when there's an error related to Google Pay integration.
 *
 * @constructor Creates a GooglePayException with the specified displayable message.
 */
@Suppress("MaxLineLength")
sealed class GooglePayException(displayableMessage: String) : SdkException(displayableMessage) {

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
     * Exception thrown when there is an initialization error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an InitialisationException with the specified displayable message.
     */
    class InitialisationException(
        displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.INITIALISATION_ERROR
    ) :
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
    class CancellationException(
        displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.CANCELLATION_ERROR
    ) : GooglePayException(displayableMessage)

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
    class ParseException(displayableMessage: String, val errorBody: String?) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is an error during the initialisation of the Google Pay wallet token.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an InitialisationWalletTokenException with the specified displayable message.
     */
    class InitialisationWalletTokenException(displayableMessage: String) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : GooglePayException(displayableMessage)
}
