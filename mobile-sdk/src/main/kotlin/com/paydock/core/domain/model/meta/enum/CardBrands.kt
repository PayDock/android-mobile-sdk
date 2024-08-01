package com.paydock.core.domain.model.meta.enum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents different card brands.
 */
@Serializable
enum class CardBrands {
    /**
     * Mastercard brand.
     */
    @SerialName("mastercard")
    MASTERCARD,

    /**
     * Maestro brand.
     */
    @SerialName("maestro")
    MAESTRO,

    /**
     * Visa brand.
     */
    @SerialName("visa")
    VISA,

    /**
     * American Express brand.
     */
    @SerialName("amex")
    AMEX,

    /**
     * Discover brand.
     */
    @SerialName("discover")
    DISCOVER
}
