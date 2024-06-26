package com.paydock.core.data.network.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing a summary of an error.
 *
 * @property message The error message.
 * @property code The error code.
 * @property details Details of the error summary.
 */
@Serializable
data class ErrorSummary(
    val message: String,
    val code: String,
    @SerialName("status_code") val statusCode: String? = null,
    @SerialName("status_code_description") val statusCodeDescription: String? = null,
    val details: ErrorSummaryDetails? = null
)