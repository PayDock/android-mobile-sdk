package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Represents an exception related to card details operations.
 *
 * @constructor Creates a CardDetailsException with the specified displayable message.
 */
sealed class CardDetailsException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when an error occurs while retrieving card schemas.
     * This exception extends [CardDetailsException] and includes an `ApiErrorResponse`
     * that contains information about the error.
     *
     * @param error The [ApiErrorResponse] that contains the error details. The `displayableMessage`
     *              from the error will be passed to the parent exception class, [CardDetailsException].
     * @constructor Initializes the exception with the given error response.
     */
    data class GetCardSchemasException(
        val error: ApiErrorResponse
    ) : CardDetailsException(error.displayableMessage)

    /**
     * Exception thrown when there is an error tokenising a card.
     *
     * @param error The underlying error response causing this exception.
     * @constructor Creates a TokenisingCardException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class TokenisingCardException(
        val error: ApiErrorResponse
    ) : CardDetailsException(error.displayableMessage)

    /**
     * Exception thrown when there is an unknown error related to Card Details.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : CardDetailsException(displayableMessage)
}
