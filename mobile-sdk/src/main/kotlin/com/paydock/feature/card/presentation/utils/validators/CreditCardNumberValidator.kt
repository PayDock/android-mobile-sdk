package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
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
     * This function checks if the credit card number is empty, applies the Luhn algorithm
     * to validate the card number's checksum, and determines the appropriate error state.
     * It also validates if the card schemes matches any of the supported card schemes.
     *
     * @param cardNumber The credit card number to validate.
     * @param hasUserInteracted Flag indicating if the user has interacted with the input field.
     * @param cardScheme The detected card scheme type.
     * @param supportedCardSchemes The list of supported card schemes.
     * @return A [CardNumberError] representing the validation result:
     *         - [CardNumberError.Empty]: The input is blank and the user has interacted.
     *         - [CardNumberError.InvalidLuhn]: The input fails the Luhn algorithm validation.
     *         - [CardNumberError.UnsupportedCardScheme]: The card scheme is not supported.
     *         - [CardNumberError.None]: The input is valid.
     */
    fun validateCardNumberInput(
        cardNumber: String,
        hasUserInteracted: Boolean,
        cardScheme: CardScheme? = null,
        supportedCardSchemes: List<CardScheme>? = null
    ): CardNumberError {
        val isLuhnValid = LuhnValidator.isLuhnValid(cardNumber)
        return when {
            cardNumber.isBlank() && hasUserInteracted -> CardNumberError.Empty
            cardNumber.isNotBlank() && !isLuhnValid -> CardNumberError.InvalidLuhn
            cardNumber.isNotBlank() && supportedCardSchemes?.isNotEmpty() == true &&
                !supportedCardSchemes.any { it == cardScheme } -> CardNumberError.UnsupportedCardScheme

            else -> CardNumberError.None
        }
    }
}