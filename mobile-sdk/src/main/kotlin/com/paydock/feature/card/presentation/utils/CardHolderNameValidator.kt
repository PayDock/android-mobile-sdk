package com.paydock.feature.card.presentation.utils

/**
 * A utility object for validating and parsing card holder name details.
 */
internal object CardHolderNameValidator {

    /**
     * Checks if the provided cardholder name is valid.
     *
     * A valid cardholder name should meet the following conditions:
     * 1. It should not be blank or empty.
     *
     * @param name The cardholder name to be validated.
     * @return `true` if the name is valid, `false` otherwise.
     */
    fun checkHolderName(name: String): Boolean =
        name.isNotBlank()

}