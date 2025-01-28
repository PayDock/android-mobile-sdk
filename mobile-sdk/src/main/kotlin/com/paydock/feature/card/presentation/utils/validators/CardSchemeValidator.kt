package com.paydock.feature.card.presentation.utils.validators

import com.paydock.api.management.domain.model.CardSchema
import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import java.util.TreeMap

/**
 * An object responsible for validating and detecting the card scheme based on a given card number.
 * It contains methods to check the validity of a card number against a collection of `CardSchema` entities.
 *
 * The `detectCardScheme` function is the main entry point to identify the card scheme for a given card number.
 */
internal object CardSchemeValidator {

    /**
     * Detects the card scheme for a given card number by validating the number against a collection
     * of `CardSchema` entities.
     *
     * This function calls [validateCardNumber] to perform the validation and returns the corresponding
     * `CardScheme` if a match is found, or `null` if the card number is invalid.
     *
     * @param collection A `TreeMap` of `CardSchema` entities, where the key is the start number of the BIN
     *                   and the value is the `CardSchema` that corresponds to that range.
     * @param number The card number to validate and detect the scheme for.
     * @return The detected [CardScheme] corresponding to the card number, or `null` if no match is found.
     */
    fun detectCardScheme(collection: TreeMap<Int, CardSchema>, number: String): CardScheme? {
        return validateCardNumber(collection, number)
    }

    /**
     * Validates the provided card number against a collection of `CardSchema` entities to determine
     * the corresponding card scheme.
     *
     * This function compares the prefix of the card number with the `start` and `end` values of the BIN ranges
     * in the `CardSchema` entities to identify the matching scheme.
     *
     * @param binTreeMap A `TreeMap` of `CardSchema` entities, where the key is the start of the BIN range
     *                   and the value is the corresponding `CardSchema`.
     * @param cardNumber The card number to validate.
     * @return The corresponding [CardScheme] if a match is found, or `null` if no match is found.
     */
    private fun validateCardNumber(
        binTreeMap: TreeMap<Int, CardSchema>,
        cardNumber: String
    ): CardScheme? {
        if (cardNumber.isEmpty()) return null

        for (entry in binTreeMap.values) {
            val prefixLength = entry.start.length
            if (cardNumber.length < prefixLength) continue

            val cardPrefix = cardNumber.substring(0, prefixLength)

            if (cardPrefix >= entry.start &&
                cardPrefix <= entry.end
            ) {
                return entry.schema
            }
        }
        return null
    }
}