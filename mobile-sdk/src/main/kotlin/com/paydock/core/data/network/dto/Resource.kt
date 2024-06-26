package com.paydock.core.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Represents the resource object containing response data.
 *
 * @property data response data.
 * @property type The type of the resource.
 */
@Serializable
data class Resource<T>(
    val type: String,
    val data: T? = null
)