package com.paydock.core.domain.error.exceptions

import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.displayableMessage
import java.io.IOException

/**
 * Represents the error that occurred while communicating with a REST API.
 *
 * @property error The underlying error response that caused this exception.
 * @constructor Creates an ApiException with the specified error response.
 *              The displayable message is derived from the error response.
 */
data class ApiException(
    val error: ApiErrorResponse
) : IOException(error.displayableMessage)