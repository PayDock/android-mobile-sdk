package com.paydock.core.domain.model.meta.enums

import kotlinx.serialization.Serializable

/**
 * Represents the available services in Mastercard SRC.
 */
@Serializable
enum class Services {
    /**
     * Inline checkout service.
     */
    INLINE_CHECKOUT,

    /**
     * Inline installments service.
     */
    INLINE_INSTALLMENTS
}
