package com.paydock.feature.src.domain.model.ui

import kotlinx.serialization.Serializable

/**
 * Represents event data with optional type information.
 *
 * @property type The type of the event data.
 */
@Serializable
internal data class EventData(
    val type: String? = null
)