package com.paydock.feature.card.presentation.state

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.ui.CardSchema
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.presentation.utils.validators.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.validators.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.validators.CardSchemeValidator
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardNumberValidator
import java.util.TreeMap

/**
 * Represents the input state for card details, including validation and metadata extraction.
 *
 * This class encapsulates the data and logic required to validate and process user-entered card details.
 * It provides utility functions to detect card scheme type, validate inputs, and extract card expiry information.
 *
 * @property cardholderName The name of the cardholder. Can be `null` if cardholder name is not collected.
 * @property cardNumber The entered card number. Defaults to an empty string.
 * @property expiry The entered expiry date of the card in MMYY format. Defaults to an empty string.
 * @property code The entered security code (CVV/CVC). Defaults to an empty string.
 * @property collectCardholderName A flag indicating whether the cardholder name is required. Defaults to `true`.
 * @property saveCard A flag indicating whether the user wants to save the card details for future use. Defaults to `false`.
 * @property schemeConfig Configuration for supported card schemes and scheme validation behavior.
 * @property cardSchemas A [TreeMap] containing the available card schemas used for validation. Defaults to an empty map.
 */
internal data class CardDetailsInputState(
    val cardholderName: String? = null,
    val cardNumber: String = "",
    val expiry: String = "",
    val code: String = "",
    val collectCardholderName: Boolean = true,
    val saveCard: Boolean = false,
    val schemeConfig: SupportedSchemeConfig = SupportedSchemeConfig(),
    val cardSchemas: TreeMap<Int, CardSchema> = TreeMap()
) {

    /**
     * Detects the card scheme type based on the entered card number.
     *
     * Uses the `CardSchemeValidator` utility to identify the type of card (e.g., Visa, Mastercard, etc.).
     */
    val cardScheme: CardScheme?
        get() = CardSchemeValidator.detectCardScheme(cardSchemas, cardNumber)

    /**
     * Extracts the expiry month from the entered expiry string.
     *
     * @return The first two characters of the expiry string, representing the month.
     * If the expiry string is shorter than the expected size, returns the available part of the string.
     */
    val expiryMonth: String
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
    val expiryYear: String
        get() = if (expiry.length >= MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH) {
            expiry.drop(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
                .take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
        } else {
            ""
        }

    /**
     * Indicates whether the current card input data is valid.
     *
     * This property performs a comprehensive validation of the card input data,
     * including the cardholder name (if required), card number, expiry date, and security code.
     * It leverages the respective validator classes to ensure each field meets the required criteria.
     *
     * The validation process includes:
     * - **Cardholder Name Validation:** If `collectCardholderName` is `true`, the cardholder name is
     *   validated using [CardHolderNameValidator.validateHolderNameInput].
     *   If `collectCardholderName` is `false`, the cardholder name is considered valid regardless of its content.
     * - **Card Number Validation:** The card number is validated using [CreditCardNumberValidator.validateCardNumberInput],
     *   which checks for emptiness, the Luhn algorithm, length, and supported card scheme.
     * - **Expiry Date Validation:** The expiry date is validated using [CardExpiryValidator.validateExpiryInput],
     *   which checks for the correct format and whether the date is in the future.
     * - **Security Code Validation:** The security code is validated using [CardSecurityCodeValidator.validateSecurityCodeInput],
     *   which checks for emptiness and the correct length based on the card scheme.
     *
     * @return `true` if all input fields are valid; `false` otherwise.
     */
    val isDataValid: Boolean
        get() {
            val isCardHolderNameValid = CardHolderNameValidator.isCardHolderNameValid(cardholderName) || !collectCardholderName
            val isCardNumberValid = CreditCardNumberValidator.isCardNumberValid(
                cardNumber,
                cardScheme,
                schemeConfig
            )
            val isExpiryValid = CardExpiryValidator.isExpiryValid(expiry)
            val isSecurityCodeValid = CardSecurityCodeValidator.isSecurityCodeValid(code, cardScheme?.code)

            return isCardHolderNameValid && isCardNumberValid && isExpiryValid && isSecurityCodeValid
        }
}