package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Represents an exception related to gift card operations.
 *
 * @constructor Creates a GiftCardException with the specified displayable message.
 */
sealed class GiftCardException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error tokenising a gift card.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a TokenisingCardException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class TokenisingCardException(
        val error: ApiErrorResponse
    ) : GiftCardException(error.displayableMessage)

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
    class ParseException(displayableMessage: String, val errorBody: String?) : GiftCardException(displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Gift Card Details.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : GiftCardException(displayableMessage)
}
