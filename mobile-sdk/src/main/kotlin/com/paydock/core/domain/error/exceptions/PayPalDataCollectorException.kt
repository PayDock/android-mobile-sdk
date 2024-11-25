package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * A sealed class representing different types of exceptions that can occur during
 * the initialization and operation of the PayPalDataCollector.
 *
 * @param displayableMessage A human-readable message describing the exception.
 */
sealed class PayPalDataCollectorException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception that occurs when there is an issue retrieving the PayPal client ID.
     *
     * @property error The API error response containing details about the failure.
     * @constructor Creates an instance of [InitialisationClientIdException] with the given error.
     */
    data class InitialisationClientIdException(val error: ApiErrorResponse) :
        PayPalDataCollectorException(error.displayableMessage)

    /**
     * A generic exception that occurs when an unknown error is encountered.
     *
     * @param displayableMessage A message providing details about the unknown error.
     * @constructor Creates an instance of [UnknownException].
     */
    class UnknownException(displayableMessage: String) :
        PayPalDataCollectorException(displayableMessage)
}
