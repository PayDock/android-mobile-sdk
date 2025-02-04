package com.paydock.feature.card.domain.model.integration.enums

/**
 * Represents supported card schemes in the system.
 */
enum class CardType {
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
    VISA;

    companion object {
        /**
         * Returns the human-readable display label for the given [CardType].
         *
         * This function maps card types to their corresponding display names,
         * which can be used for UI purposes.
         *
         * @receiver The [CardType] for which the display label is needed.
         * @return A string representing the card's display name.
         *
         * Example usage:
         * ```
         * val label = CardType.VISA.displayLabel() // Returns "Visa"
         * val amexLabel = CardType.AMEX.displayLabel() // Returns "American Express"
         * ```
         */
        internal fun CardType.displayLabel(): String {
            return when (this) {
                AMEX -> "American Express"
                AUSBC -> "Australian Bank Card"
                DINERS -> "Diners Club"
                DISCOVER -> "Discover"
                JAPCB -> "JCB"
                MASTERCARD -> "MasterCard"
                SOLO -> "Solo"
                VISA -> "Visa"
            }
        }
    }

}