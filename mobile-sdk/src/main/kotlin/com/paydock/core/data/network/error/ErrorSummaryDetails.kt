package com.paydock.core.data.network.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing details of an error summary.
 *
 * @property gatewaySpecificCode Specific code related to the gateway (nullable).
 * @property gatewaySpecificDescription Specific description related to the gateway (nullable).
 * @property messages List of additional messages related to the error (nullable list).
 * @property description Description of the error (nullable).
 * @property statusCode Status code related to the error (nullable).
 * @property statusCodeDescription Description of the status code (nullable).
 */
@Serializable
data class ErrorSummaryDetails(
    @SerialName("gateway_specific_code") val gatewaySpecificCode: String? = null,
    @SerialName("gateway_specific_description") val gatewaySpecificDescription: String? = null,
    val messages: List<String>? = null,
    val description: String? = null,
    @SerialName("status_code") val statusCode: String? = null,
    @SerialName("status_code_description") val statusCodeDescription: String? = null
)