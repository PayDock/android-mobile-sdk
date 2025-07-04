package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Exception thrown when there's an error related to Afterpay integration.
 *
 * @property displayableMessage A human-readable message describing the error.
 * @constructor Creates an AfterpayException with the specified displayable message.
 */
sealed class AfterpayException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error fetching the URL for Afterpay.
     *
     * @property error The underlying error response causing this exception.
     */
    data class FetchingUrlException(
        val error: ApiErrorResponse
    ) : AfterpayException(error.displayableMessage)

    /**
     * Exception thrown when there is an error capturing the charge for Afterpay.
     *
     * @property error The underlying error response causing this exception.
     */
    data class CapturingChargeException(
        val error: ApiErrorResponse
    ) : AfterpayException(error.displayableMessage)

    /**
     * Exception thrown when there is an error with the Afterpay token.
     *
     * @param displayableMessage A human-readable message describing the error.
     */
    class TokenException(displayableMessage: String) : AfterpayException(displayableMessage)

    /**
     * Exception thrown when there is a configuration error related to Afterpay.
     *
     * @param displayableMessage A human-readable message describing the error.
     */
    class ConfigurationException(displayableMessage: String) : AfterpayException(displayableMessage)

    /**
     * Exception thrown when there is a cancellation error related to Afterpay.
     *
     * @param displayableMessage A human-readable message describing the error.
     */
    class CancellationException(displayableMessage: String) : AfterpayException(displayableMessage)

    /**
     * Exception thrown when there is an invalid intent result error related to Afterpay.
     *
     * @param displayableMessage A human-readable message describing the error.
     */
    class InvalidResultException(displayableMessage: String) : AfterpayException(displayableMessage)

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
    class ParseException(displayableMessage: String, val errorBody: String?) : AfterpayException(displayableMessage)

    /**
     * Exception thrown when there is an error during the initialisation of the Afterpay wallet token.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an InitialisationWalletTokenException with the specified displayable message.
     */
    class InitialisationWalletTokenException(displayableMessage: String) : AfterpayException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Afterpay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : AfterpayException(displayableMessage)
}
