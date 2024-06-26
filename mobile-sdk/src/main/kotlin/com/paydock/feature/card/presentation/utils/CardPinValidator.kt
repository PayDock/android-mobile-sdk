package com.paydock.feature.card.presentation.utils

/**
 * A utility object for validating and parsing card pin details.
 */
internal object CardPinValidator {

    /**
     * Checks if the provided card pin is valid.
     *
     * A valid card pin should meet the following conditions:
     * 1. It should not be blank or empty.
     * 2. It should contain only digits.
     *
     * @param pin The card pin to be validated.
     * @return `true` if the name is valid, `false` otherwise.
     */
    fun checkPin(pin: String): Boolean =
        pin.isNotBlank() && pin.matches(Regex("^[0-9]+$"))

}