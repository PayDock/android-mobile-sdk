package com.paydock.feature.card.presentation.state

import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.presentation.utils.validators.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.validators.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.validators.CardSchemeValidator
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardNumberValidator

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
 * This value is used directly for the `saved_card_consent_accepted` field in the tokenization request.
 * @property storeSecurityCode Specifies whether the security code (CVV) should be saved when tokenizing a card.
 * If `null`, the `store_ccv` parameter will not be sent in the tokenization request.
 * @property schemeConfig Configuration for supported card schemes and scheme validation behavior.
 * @property binData The BIN data used for card scheme detection (cache first, then bundled asset). Defaults to `null`.
 * @property ctaErrorsOverwrite A flag indicating whether to force display validation errors for all fields. Defaults to `false`.
 */
internal data class CardDetailsInputState(
    val cardholderName: String? = null,
    val cardNumber: String = "",
    val expiry: String = "",
    val code: String = "",
    val collectCardholderName: Boolean = true,
    val saveCard: Boolean = false,
    val storeSecurityCode: Boolean? = null,
    val schemeConfig: SupportedSchemeConfig = SupportedSchemeConfig(),
    val binData: BinDataResponse? = null,
    val cardholderNameErrorOverwrite: Boolean = false,
    val cardNumberErrorOverwrite: Boolean = false,
    val cardExpiryErrorOverwrite: Boolean = false,
    val cardSecurityErrorOverwrite: Boolean = false
) {

    /**
     * Detects the card scheme type based on the entered card number.
     *
     * Uses the `CardSchemeValidator` utility to identify the type of card (e.g., Visa, Mastercard, etc.).
     */
    val cardScheme: CardScheme?
        get() = CardSchemeValidator.detectCardScheme(binData, cardNumber)

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
     * Enum representing the input fields in the card details form.
     */
    enum class CardField {
        CARDHOLDER_NAME,
        CARD_NUMBER,
        EXPIRY,
        SECURITY_CODE
    }

    /**
     * Returns a list of invalid fields based on the current state.
     */
    val invalidFields: List<CardField>
        get() = mutableListOf<CardField>().apply {
            if (collectCardholderName && !CardHolderNameValidator.isCardHolderNameValid(cardholderName ?: "")) {
                add(CardField.CARDHOLDER_NAME)
            }
            if (!CreditCardNumberValidator.isCardNumberValid(cardNumber, cardScheme, schemeConfig)) {
                add(CardField.CARD_NUMBER)
            }
            if (!CardExpiryValidator.isExpiryValid(expiry)) {
                add(CardField.EXPIRY)
            }
            if (!CardSecurityCodeValidator.isSecurityCodeValid(code, cardScheme?.code)) {
                add(CardField.SECURITY_CODE)
            }
        }

    /**
     * Returns the total number of validation errors in the form.
     */
    val errorCount: Int
        get() = invalidFields.size

    /**
     * Indicates whether the current card input data is valid.
     */
    val isDataValid: Boolean
        get() = errorCount == 0
}