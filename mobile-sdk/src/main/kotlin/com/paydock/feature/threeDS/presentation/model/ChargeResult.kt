package com.paydock.feature.threeDS.presentation.model

import kotlinx.serialization.Serializable

/**
 * Serializable data class representing the result of a charge, typically used during decoupled challenge flows.
 *
 * @property description The description field to be shown to the customer,
 * indicating the method the user must use to authenticate during the decoupled challenge flow.
 */
@Serializable
internal data class ChargeResult(
    val description: String? = null
)
