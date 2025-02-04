package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.CardSchema
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import java.util.TreeMap

/**
 * An object responsible for validating and detecting the card scheme based on a given card number.
 * It contains methods to check the validity of a card number against a collection of `CardSchema` entities.
 *
 * The `detectCardScheme` function is the main entry point to identify the card scheme for a given card number.
 */
internal object CardSchemeValidator {

    private val schemes = listOf(
        CardScheme(
            type = CardType.VISA,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16, 18, 19),
            code = CardCode(type = CodeType.CVV, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.MASTERCARD,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16),
            code = CardCode(type = CodeType.CVC, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.AMEX,
            gaps = listOf(4, 10),
            lengths = listOf(15),
            code = CardCode(type = CodeType.CID, size = MobileSDKConstants.CardDetailsConfig.CID_LENGTH)
        ),

        CardScheme(
            type = CardType.DINERS,
            gaps = listOf(4, 10),
            lengths = listOf(14),
            code = CardCode(type = CodeType.CVV, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.DISCOVER,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16, 19),
            code = CardCode(type = CodeType.CID, size = MobileSDKConstants.CardDetailsConfig.CID3_LENGTH)
        ),

        CardScheme(
            type = CardType.JAPCB,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16),
            code = CardCode(type = CodeType.CVV, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.SOLO,
            gaps = emptyList(),
            lengths = listOf(12, 19),
            code = CardCode(type = CodeType.CVC, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.AUSBC,
            gaps = emptyList(),
            lengths = listOf(12, 19),
            code = CardCode(type = CodeType.CVC, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        )
    )

    /**
     * Detects the card scheme for a given card number by validating the number against a collection
     * of `CardSchema` entities.
     *
     * This function calls [validateCardNumber] to perform the validation and returns the corresponding
     * `CardScheme` if a match is found, or `null` if the card number is invalid.
     *
     * @param binRanges A `TreeMap` of `CardSchema` entities, where the key is the start number of the BIN
     *                   and the value is the `CardSchema` that corresponds to that range.
     * @param number The card number to validate and detect the scheme for.
     * @return The detected [CardScheme] corresponding to the card number, or `null` if no match is found.
     */
    fun detectCardScheme(binRanges: TreeMap<Int, CardSchema>, number: String): CardScheme? {
        return validateCardNumber(binRanges, number)
    }

    /**
     * Validates the provided card number against a collection of `CardSchema` entities to determine
     * the corresponding card scheme.
     *
     * This function compares the prefix of the card number with the `start` and `end` values of the BIN ranges
     * in the `CardSchema` entities to identify the matching scheme.
     *
     * @param binRanges A `TreeMap` of `CardSchema` entities, where the key is the start of the BIN range
     *                   and the value is the corresponding `CardSchema`.
     * @param cardNumber The card number to validate.
     * @return The corresponding [CardScheme] if a match is found, or `null` if no match is found.
     */
    private fun validateCardNumber(
        binRanges: TreeMap<Int, CardSchema>,
        cardNumber: String
    ): CardScheme? {
        if (cardNumber.isEmpty()) return null

        // Optimized: Iterate over entries directly for better performance
        for ((_, entry) in binRanges) {
            val prefixLength = entry.start.length
            //  Early exit if card number is too short
            if (cardNumber.length < prefixLength) continue

            val cardPrefix = cardNumber.substring(0, prefixLength)

            if (cardPrefix >= entry.start && cardPrefix <= entry.end) {
                return schemes.find { it.type == entry.schema }
            }
        }
        return null
    }
}