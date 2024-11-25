package com.paydock.feature.src.domain.model.integration.meta.enum

import kotlinx.serialization.Serializable

/**
 * Represents the preferences for shipping and billing in Click to Pay DPA.
 */
@Serializable
enum class ClickToPayDPAShippingBillingPreference {
    /**
     * Use full shipping and billing information.
     */
    FULL,

    /**
     * Use only postal country for shipping and billing.
     */
    POSTAL_COUNTRY,

    /**
     * No shipping and billing information.
     */
    NONE
}
