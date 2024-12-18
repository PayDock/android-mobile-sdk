package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants

/**
 * A utility object for validating gift card number details.
 */
internal object GiftCardNumberValidator {

    /**
     * Checks if the provided gift card number is valid.
     *
     * A valid credit card number should meet the following conditions:
     * 1. It should not be blank or empty.
     * 2. It should contain only digits.
     * 3. It should not exceed the maximum allowed card length defined by [MAX_GIFT_CARD_LENGTH].
     *
     * @param number The gift card number to be validated.
     * @return `true` if the number is valid, `false` otherwise.
     */
    fun isValidNumberFormat(number: String): Boolean =
        number.isNotBlank() &&
            number.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) &&
            number.length <= MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH

    /**
     * Validates whether the provided card number meets the required format and length constraints.
     *
     * The card number is considered valid if:
     * 1. It matches the expected numeric format using [isValidNumberFormat].
     * 2. Its length falls within the range defined by:
     *    - [MobileSDKConstants.CardDetailsConfig.MIN_GIFT_CARD_LENGTH]
     *    - [MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH].
     *
     * @param number The card number to be validated.
     * @return `true` if the card number is valid, `false` otherwise.
     *
     * Example:
     * ```
     * isCardNumberValid("123456789012") // Returns true if within the valid length and format
     * isCardNumberValid("abcd1234")     // Returns false due to invalid format
     * ```
     */
    fun isCardNumberValid(number: String): Boolean =
        isValidNumberFormat(number) &&
            number.length in
            MobileSDKConstants.CardDetailsConfig.MIN_GIFT_CARD_LENGTH..MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH

}