package com.paydock.core.domain.error.exceptions

import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.displayableMessage
import java.io.IOException

/**
 * Represents an exception related to gift card operations.
 *
 * @constructor Creates a GiftCardException with the specified displayable message.
 */
sealed class GiftCardException(displayableMessage: String) : IOException(displayableMessage) {

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
     * Exception thrown when there is an unknown error related to Gift Card Details.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : GiftCardException(displayableMessage)
}
