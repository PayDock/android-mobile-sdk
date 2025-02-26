package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.presentation.utils.errors.CardNumberError

/**
 * Utility object for validating credit card numbers.
 *
 * Provides methods to validate the format, length, and correctness of credit card numbers,
 * including Luhn algorithm validation.
 */
internal object CreditCardNumberValidator {

    /**
     * Checks if a given credit card number is valid.
     *
     * This function validates the card number by checking its format, length,
     * and Luhn algorithm correctness.
     *
     * @param cardNumber The credit card number to validate.
     * @param cardScheme The detected card scheme, if available.
     * @param schemeConfig The configuration defining the supported card schemes and validation settings.
     * @return `true` if the card number is valid, `false` otherwise.
     */
    fun isCardNumberValid(
        cardNumber: String,
        cardScheme: CardScheme?,
        schemeConfig: SupportedSchemeConfig
    ) = validateCardNumberInput(cardNumber, cardScheme, schemeConfig, true) == CardNumberError.None

    /**
     * Validates the credit card number input and determines the type of validation error.
     *
     * This function applies the following validations in order:
     * 1. Checks if the input is blank and the user has interacted, returning [CardNumberError.Empty].
     * 2. Validates the input using the Luhn algorithm, returning [CardNumberError.InvalidLuhn] if invalid.
     * 3. Checks if the card scheme is among the supported schemes (if validation is enabled),
     *    returning [CardNumberError.UnsupportedCardScheme] if unsupported.
     * 4. Checks if the card number length is valid for the given scheme, returning [CardNumberError.InvalidLength] if invalid.
     * 5. If all validations pass, it returns [CardNumberError.None].
     *
     * @param cardNumber The credit card number to validate.
     * @param cardScheme The detected card scheme, if available.
     * @param schemeConfig The configuration defining the supported card schemes and validation settings.
     * @param hasUserInteracted Flag indicating if the user has interacted with the input field.
     * @return A [CardNumberError] representing the validation result.
     */
    fun validateCardNumberInput(
        cardNumber: String,
        cardScheme: CardScheme?,
        schemeConfig: SupportedSchemeConfig,
        hasUserInteracted: Boolean,
    ): CardNumberError {
        val isLuhnValid = LuhnValidator.isLuhnValid(cardNumber)
        val supportedCardSchemes = getSupportedCardSchemes(schemeConfig)
        val cardType = cardScheme?.type
        val isValidLength = validateCardNumberLength(cardScheme, cardNumber)
        // Determine if the card scheme is supported (only if validation is enabled)
        val isCardSchemeSupported = supportedCardSchemes?.let {
            it.isNotEmpty() && cardType != null && it.contains(cardType)
        } ?: true // Default to true if validation is disabled or schemes are not provided
        return when {
            cardNumber.isBlank() && hasUserInteracted -> CardNumberError.Empty
            cardNumber.isNotBlank() && !isLuhnValid -> CardNumberError.InvalidLuhn
            cardNumber.isNotBlank() && !isValidLength -> CardNumberError.InvalidLength
            cardNumber.isNotBlank() && !isCardSchemeSupported -> CardNumberError.UnsupportedCardScheme
            else -> CardNumberError.None
        }
    }

    /**
     * Validates the length of a credit card number based on its card scheme.
     *
     * @param cardScheme The detected card scheme, if available.
     * @param cardNumber The credit card number to validate.
     * @return `true` if the card number length is valid for the given scheme, `false` otherwise.
     */
    private fun validateCardNumberLength(cardScheme: CardScheme?, cardNumber: String): Boolean {
        val length = cardNumber.length
        val (minLength, maxLength) = getCardSchemeLengthRange(cardScheme?.lengths ?: emptyList())
        return length in minLength..maxLength
    }

    /**
     * Returns the valid length range (min and max) for a given list of valid card lengths.
     *
     * If the provided list is empty, default length constraints are used.
     *
     * @param lengths The list of valid lengths for the card scheme.
     * @return A Pair representing the minimum and maximum valid lengths.
     */
    private fun getCardSchemeLengthRange(lengths: List<Int>): Pair<Int, Int> {
        return if (lengths.isNotEmpty()) {
            lengths.min() to lengths.max()
        } else {
            MobileSDKConstants.CardDetailsConfig.MIN_CREDIT_CARD_LENGTH to MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH
        }
    }

    /**
     * Retrieves the set of supported card schemes based on the provided configuration.
     *
     * If validation is enabled, returns the set of supported card schemes.
     * Otherwise, returns `null`, indicating that all card schemes are accepted without validation.
     *
     * @param schemeConfig The configuration specifying whether validation is enabled and the supported card schemes.
     * @return A set of supported [CardType]s if validation is enabled, or `null` if validation is disabled.
     */
    private fun getSupportedCardSchemes(schemeConfig: SupportedSchemeConfig): Set<CardType>? {
        return if (schemeConfig.enableValidation) schemeConfig.supportedSchemes else null
    }
}