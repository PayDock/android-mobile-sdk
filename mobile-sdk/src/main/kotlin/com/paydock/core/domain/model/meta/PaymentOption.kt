package com.paydock.core.domain.model.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a payment option.
 *
 * @property dynamicDataType Dynamic data types.
 */
@Serializable
data class PaymentOption(
    @SerialName("dynamic_data_type") val dynamicDataType: String?
)
