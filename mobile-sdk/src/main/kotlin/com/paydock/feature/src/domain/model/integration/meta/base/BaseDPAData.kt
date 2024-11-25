package com.paydock.feature.src.domain.model.integration.meta.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the base data for a Direct Payment Application (DPA).
 *
 * @property dpaPresentationName The name in which the DPA is presented.
 * @property dpaUri Used for indicating the DPA URI.
 */
@Serializable
abstract class BaseDPAData(
    @SerialName("dpa_presentation_name") val dpaPresentationName: String? = null,
    open val dpaUri: String? = null
)
