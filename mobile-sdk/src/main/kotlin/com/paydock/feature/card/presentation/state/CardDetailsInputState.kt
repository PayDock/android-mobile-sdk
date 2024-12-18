package com.paydock.feature.card.presentation.state

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.enums.CardIssuerType
import com.paydock.feature.card.domain.model.integration.enums.SecurityCodeType
import com.paydock.feature.card.presentation.utils.validators.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.validators.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.validators.CardIssuerValidator
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardNumberValidator

/**
 * Represents the input state for card details, including validation and metadata extraction.
 *
 * This class encapsulates the data and logic required to validate and process user-entered card details.
 * It provides utility functions to detect card issuer type, validate inputs, and extract card expiry information.
 *
 * @property cardholderName The name of the cardholder. Can be `null` if cardholder name is not collected.
 * @property cardNumber The entered card number. Defaults to an empty string.
 * @property expiry The entered expiry date of the card in MMYY format. Defaults to an empty string.
 * @property code The entered security code (CVV/CVC). Defaults to an empty string.
 * @property collectCardholderName A flag indicating whether the cardholder name is required. Defaults to `true`.
 * @property saveCard A flag indicating whether the user wants to save the card details for future use. Defaults to `false`.
 */
internal data class CardDetailsInputState(
    val cardholderName: String? = null,
    val cardNumber: String = "",
    val expiry: String = "",
    val code: String = "",
    val collectCardholderName: Boolean = true,
    val saveCard: Boolean = false
) {

    /**
     * Detects the card issuer type based on the entered card number.
     *
     * Uses the `CardIssuerValidator` utility to identify the type of card (e.g., Visa, Mastercard, etc.).
     */
    private val cardIssuer: CardIssuerType
        get() = CardIssuerValidator.detectCardIssuer(cardNumber)

    /**
     * Determines the security code type (CVV/CVC) based on the detected card issuer.
     *
     * Uses the `CardSecurityCodeValidator` utility to identify the expected security code format for the card type.
     */
    private val securityCodeType: SecurityCodeType
        get() = CardSecurityCodeValidator.detectSecurityCodeType(cardIssuer)

    /**
     * Validates the entire input state.
     *
     * @return `true` if all inputs are valid; `false` otherwise.
     * - Validates the cardholder name (if required).
     * - Checks the card number using the Luhn algorithm.
     * - Validates the card expiry date format and logic.
     * - Validates the security code based on the detected card issuer.
     */
    val isDataValid: Boolean
        get() = (CardHolderNameValidator.checkHolderName(cardholderName) || !collectCardholderName) &&
            CreditCardNumberValidator.isValidNumberFormat(cardNumber) &&
            CreditCardInputValidator.isLuhnValid(cardNumber) &&
            CardExpiryValidator.isExpiryValid(expiry) &&
            CardSecurityCodeValidator.isSecurityCodeValid(code, securityCodeType)

    /**
     * Extracts the expiry month from the entered expiry string.
     *
     * @return The first two characters of the expiry string, representing the month.
     * If the expiry string is shorter than the expected size, returns the available part of the string.
     */
    internal val expiryMonth: String
        get() = if (expiry.length >= MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE) {
            expiry.take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
        } else {
            expiry
        }

    /**
     * Extracts the expiry year from the entered expiry string.
     *
     * @return The last two characters of the expiry string, representing the year.
     * If the expiry string is shorter than the expected size, returns an empty string.
     */
    internal val expiryYear: String
        get() = if (expiry.length >= MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH) {
            expiry.drop(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
                .take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
        } else {
            ""
        }
}