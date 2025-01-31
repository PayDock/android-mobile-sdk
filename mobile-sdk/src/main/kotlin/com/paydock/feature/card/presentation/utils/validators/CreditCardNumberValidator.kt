package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import com.paydock.feature.card.presentation.utils.errors.CardNumberError

/**
 * Utility object for validating credit card numbers.
 *
 * Provides methods to validate the format, length, and correctness of credit card numbers,
 * including Luhn algorithm validation.
 */
internal object CreditCardNumberValidator {

    /**
     * Checks if the provided credit card number meets the basic validation criteria.
     *
     * A valid credit card number should:
     * 1. Be non-blank.
     * 2. Contain only numeric characters.
     * 3. Not exceed the maximum allowed length as defined by
     *    [MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH].
     *
     * @param number The credit card number to validate.
     * @return `true` if the credit card number meets the basic validation criteria, otherwise `false`.
     */
    fun isValidNumberFormat(number: String): Boolean =
        number.isNotBlank() && number.length <= MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH &&
            number.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS)

    /**
     * Validates the credit card number input and determines the type of validation error.
     *
     * This function applies the following validations in order:
     * 1. Checks if the input is blank and the user has interacted, returning [CardNumberError.Empty].
     * 2. Validates the input using the Luhn algorithm, returning [CardNumberError.InvalidLuhn] if invalid.
     * 3. Checks if the card scheme is among the supported schemes (if validation is enabled),
     *    returning [CardNumberError.UnsupportedCardScheme] if unsupported.
     * 4. If all validations pass, it returns [CardNumberError.None].
     *
     * @param cardNumber The credit card number to validate.
     * @param hasUserInteracted Flag indicating if the user has interacted with the input field.
     * @param cardScheme The detected card scheme type. Defaults to `null`.
     * @param schemeConfig The configuration defining the supported card schemes and validation settings.
     * @return A [CardNumberError] representing the validation result.
     */
    fun validateCardNumberInput(
        cardNumber: String,
        hasUserInteracted: Boolean,
        cardScheme: CardScheme? = null,
        schemeConfig: SupportedSchemeConfig,
    ): CardNumberError {
        val isLuhnValid = LuhnValidator.isLuhnValid(cardNumber)
        val supportedCardSchemes = getSupportedCardSchemes(schemeConfig)
        val isValidLength = validateCardNumberLength(cardScheme, cardNumber)
        // Determine if the card scheme is supported (only if validation is enabled)
        val isCardSchemeSupported = supportedCardSchemes?.let {
            it.isNotEmpty() && cardScheme != null && it.contains(cardScheme)
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
     * @param cardScheme The card scheme (e.g., Visa, Mastercard).
     * @param cardNumber The credit card number to validate.
     * @return `true` if the card number length is valid for the given scheme, `false` otherwise.
     */
    private fun validateCardNumberLength(cardScheme: CardScheme?, cardNumber: String): Boolean {
        val length = cardNumber.length
        val (minLength, maxLength) = getCardSchemeLengthRange(cardScheme)
        return length in minLength..maxLength
    }

    /**
     * Returns the valid length range (min and max) for a given card scheme.
     *
     * @param cardScheme The card scheme.
     * @return A Pair representing the minimum and maximum valid lengths for the scheme.
     */
    @Suppress("MagicNumber")
    private fun getCardSchemeLengthRange(cardScheme: CardScheme?): Pair<Int, Int> {
        return when (cardScheme) {
            CardScheme.VISA -> 16 to 19
            CardScheme.MASTERCARD -> 16 to 16
            CardScheme.AMEX -> 15 to 15
            CardScheme.DINERS -> 14 to 14
            CardScheme.DISCOVER -> 16 to 19
            CardScheme.JAPCB -> 16 to 16
            CardScheme.SOLO -> 12 to 19
            CardScheme.AUSBC -> 12 to 19
            else -> 12 to 19
        }
    }

    /**
     * Retrieves the set of supported card schemes based on the provided configuration.
     *
     * This function checks if validation for card schemes is enabled in the configuration.
     * If validation is enabled, it returns the set of supported card schemes. Otherwise, it returns `null`,
     * indicating that all card schemes are accepted without validation.
     *
     * @param schemeConfig The configuration specifying whether validation is enabled and the supported card schemes.
     * @return A set of supported [CardScheme]s if validation is enabled, or `null` if validation is disabled.
     */
    private fun getSupportedCardSchemes(schemeConfig: SupportedSchemeConfig): Set<CardScheme>? {
        return if (schemeConfig.enableValidation) schemeConfig.supportedSchemes else null
    }

}