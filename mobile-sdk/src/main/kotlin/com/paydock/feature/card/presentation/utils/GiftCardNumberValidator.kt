package com.paydock.feature.card.presentation.utils

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
    fun checkNumber(number: String): Boolean =
        number.isNotBlank() &&
            number.matches(Regex("^[0-9]+$")) &&
            number.length <= MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH

    fun isCardNumberValid(number: String): Boolean = checkNumber(number) && number.length in
        MobileSDKConstants.CardDetailsConfig.MIN_GIFT_CARD_LENGTH..MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH
}