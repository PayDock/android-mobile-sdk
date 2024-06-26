package com.paydock.feature.card.presentation.state

import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.feature.card.presentation.model.CardIssuerType
import com.paydock.feature.card.presentation.model.SecurityCodeType
import com.paydock.feature.card.presentation.utils.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.CardIssuerValidator
import com.paydock.feature.card.presentation.utils.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.CreditCardInputValidator
import com.paydock.feature.card.presentation.utils.CreditCardNumberValidator

/**
 * UI State that represents card details input and processing state.
 *
 * @property cardholderName The name of the cardholder.
 * @property cardNumber The card number entered by the user.
 * @property expiry The expiry date of the card in MM/YY format.
 * @property code The security code (CVV) of the card.
 * @property gatewayId The ID of the payment gateway.
 * @property saveCard A flag indicating whether to save the card details for future use.
 * @property token A token representing the card details, if available.
 * @property isLoading A flag indicating whether the card details are currently being processed.
 * @property error An error model representing any error that occurred during card details processing.
 */
internal data class CardDetailsViewState(
    val cardholderName: String = "",
    val cardNumber: String = "",
    val expiry: String = "",
    val code: String = "",
    val gatewayId: String = "",
    val saveCard: Boolean = false,
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: CardDetailsException? = null,
) {
    /**
     * Get the card issuer type based on the entered card number.
     */
    private val cardIssuer: CardIssuerType
        get() = CardIssuerValidator.detectCardIssuer(cardNumber)

    /**
     * Get the security code type (CVV/CVC) based on the detected card issuer.
     */
    private val securityCodeType: SecurityCodeType
        get() = CardSecurityCodeValidator.detectSecurityCodeType(cardIssuer)

    /**
     * Get the validity status of all input data.
     *
     * @return True if all input data is valid, false otherwise.
     */
    val isDataValid: Boolean
        get() = CardHolderNameValidator.checkHolderName(cardholderName) &&
            CreditCardNumberValidator.checkNumber(cardNumber) &&
            CreditCardInputValidator.isLuhnValid(cardNumber) &&
            CardExpiryValidator.isExpiryValid(expiry) &&
            CardSecurityCodeValidator.isSecurityCodeValid(code, securityCodeType)

    /**
     * Extract the expiry month from the full expiry string.
     *
     * @return The month part of the expiry date.
     */
    internal val expiryMonth: String
        get() = if (expiry.length >= MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE) {
            expiry.take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
        } else {
            expiry
        }

    /**
     * Extract the expiry year from the full expiry string.
     *
     * @return The year part of the expiry date.
     */
    internal val expiryYear: String
        get() = if (expiry.length >= MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH) {
            expiry.drop(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
                .take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
        } else {
            ""
        }
}