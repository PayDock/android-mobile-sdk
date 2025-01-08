package com.paydock.feature.card.domain.model.integration.enums

/**
 * Represents supported card schemes in the system.
 */
enum class CardScheme {
    /**
     * American Express card scheme.
     */
    AMEX,

    /**
     * Australian Bankcard card scheme.
     */
    AUSBC,

    /**
     * Diners Club International card scheme.
     */
    DINERS,

    /**
     * Discover Card card scheme.
     */
    DISCOVER,

    /**
     * JCB (Japan Credit Bureau) card scheme.
     */
    JAPCB,

    /**
     * Mastercard card scheme.
     */
    MASTERCARD,

    /**
     * Solo card scheme, typically used in the UK.
     */
    SOLO,

    /**
     * Visa card scheme.
     */
    VISA
}