package com.paydock.core.domain.model.meta.enum

import kotlinx.serialization.Serializable

/**
 * Represents the available services in Click to Pay.
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
