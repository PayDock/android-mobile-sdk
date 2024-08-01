package com.paydock.feature.src.presentation.model

import kotlinx.serialization.Serializable

/**
 * Represents an error detail containing information about a specific error.
 *
 * @property field The field associated with the error. Can be null if not applicable.
 * @property message The error message describing the issue.
 */
@Serializable
internal data class ErrorDetail(
    val field: String? = null,
    val message: String
)
