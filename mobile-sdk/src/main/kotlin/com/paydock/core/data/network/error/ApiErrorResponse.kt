package com.paydock.core.data.network.error

import com.paydock.core.data.network.dto.Resource
import com.paydock.core.domain.error.exceptions.ApiException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing a generic API error response.
 *
 * @property status The HTTP status code.
 * @property error The API error.
 * @property summary The error summary.
 */
@Serializable
data class ApiErrorResponse(
    val status: Int,
    val error: ApiError? = null,
    val resource: Resource<Unit>? = null,
    @SerialName("error_summary") val summary: ErrorSummary
)

/**
 * Extension property that provides a error summary message.
 *
 * @return The displayable error message.
 */
val ApiErrorResponse?.displayableMessage: String
    get() = this?.summary?.message ?: ""

/**
 * Converts the [ApiErrorResponse] to an [ApiException].
 */
internal fun ApiErrorResponse.toApiError() = ApiException(this)