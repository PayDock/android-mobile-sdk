package com.paydock.core.domain.model.meta.enums

import kotlinx.serialization.Serializable

/**
 * Represents different checkout experiences.
 */
@Serializable
enum class CheckoutExperience {
    /**
     * The checkout experience is within the checkout process.
     */
    WITHIN_CHECKOUT,

    /**
     * The checkout experience is within the payment settings.
     */
    PAYMENT_SETTINGS
}
