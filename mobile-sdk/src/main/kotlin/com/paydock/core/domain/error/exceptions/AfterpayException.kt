package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage
import java.io.IOException

/**
 * Exception thrown when there's an error related to Afterpay integration.
 *
 * @property displayableMessage A human-readable message describing the error.
 * @constructor Creates an AfterpayException with the specified displayable message.
 */
sealed class AfterpayException(displayableMessage: String) : IOException(displayableMessage) {

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
     * Exception thrown when there is an unknown error related to Afterpay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : AfterpayException(displayableMessage)
}
