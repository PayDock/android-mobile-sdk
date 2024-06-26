package com.paydock.core.data.network.error

import kotlinx.serialization.Serializable

/**
 * Serializable data class representing an API error.
 *
 * @property message The error message.
 * @property code The error code.
 * @property details Additional details about the error (nullable).
 */
@Serializable
data class ApiError(val message: String, val code: String, val details: ErrorDetails? = null)