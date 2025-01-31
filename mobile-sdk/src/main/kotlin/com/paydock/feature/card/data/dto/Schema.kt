package com.paydock.feature.card.data.dto

import com.paydock.feature.card.data.utils.SchemaSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enumeration representing different card schemas.
 *
 * This enum class defines the supported card schemas used in the application.
 * Each schema is serialized to and from its corresponding string representation
 * in JSON using the [SchemaSerializer].
 */
@Serializable(with = SchemaSerializer::class)
internal enum class Schema {
    /**
     * American Express card schema.
     * Commonly used in the United States and globally for premium cards.
     */
    @SerialName("amex")
    AMEX,

    /**
     * Australian Bankcard card schema.
     * Historically used in Australia, though now obsolete.
     */
    @SerialName("ausbc")
    AUSBC,

    /**
     * Diners Club International card schema.
     * Recognized for its association with business and travel cards.
     */
    @SerialName("diners")
    DINERS,

    /**
     * Discover Card card schema.
     * Primarily used in the United States.
     */
    @SerialName("discover")
    DISCOVER,

    /**
     * JCB (Japan Credit Bureau) card schema.
     * Widely used in Japan and accepted globally.
     */
    @SerialName("japcb")
    JAPCB,

    /**
     * Mastercard card schema.
     * A globally recognized card network used extensively worldwide.
     */
    @SerialName("mastercard")
    MASTERCARD,

    /**
     * Solo card schema.
     * A debit card scheme formerly used in the UK, now largely discontinued.
     */
    @SerialName("solo")
    SOLO,

    /**
     * Visa card schema.
     * One of the most widely used card networks globally.
     */
    @SerialName("visa")
    VISA;

    /**
     * Companion object providing utility methods for [Schema].
     */
    companion object {
        /**
         * Retrieves a [Schema] based on its string representation.
         *
         * @param value The string representation of the schema.
         * @return The corresponding [Schema] if found, or `null` if no match is found.
         */
        fun fromValue(value: String): Schema? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}