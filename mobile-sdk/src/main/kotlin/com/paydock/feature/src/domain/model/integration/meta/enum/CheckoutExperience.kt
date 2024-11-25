package com.paydock.feature.src.domain.model.integration.meta.enum

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
