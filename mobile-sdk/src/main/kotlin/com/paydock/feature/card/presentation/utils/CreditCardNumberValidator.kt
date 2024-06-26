package com.paydock.feature.card.presentation.utils

import com.paydock.core.MobileSDKConstants

/**
 * A utility object for validating credit card number details.
 */
internal object CreditCardNumberValidator {

    /**
     * Checks if the provided credit card number is valid.
     *
     * A valid credit card number should meet the following conditions:
     * 1. It should not be blank or empty.
     * 2. It should contain only digits.
     * 3. It should not exceed the maximum allowed card length defined by [MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH].
     *
     * @param number The credit card number to be validated.
     * @return `true` if the number is valid, `false` otherwise.
     */
    fun checkNumber(number: String): Boolean =
        number.isNotBlank() && number.length <= MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH &&
            number.matches(Regex("^[0-9]+$"))

}