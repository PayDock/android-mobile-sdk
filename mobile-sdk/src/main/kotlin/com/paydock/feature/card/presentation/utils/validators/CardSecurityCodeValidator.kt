package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError

/**
 * A utility object for validating and detecting credit card security codes (CVV, CVC, CID) based on card scheme and security code type.
 */
internal object CardSecurityCodeValidator {

    /**
     * Checks if a security code is valid.
     *
     * @param code The security code input by the user.
     * @param cardCode The expected security code configuration based on the card scheme.
     * @return `true` if the security code is valid, `false` otherwise.
     */
    fun isSecurityCodeValid(code: String, cardCode: CardCode?): Boolean =
        validateSecurityCodeInput(code, cardCode, true) == SecurityCodeError.None

    /**
     * Validates the user input for a security code.
     *
     * The validation considers the following cases:
     * - If the input is blank and the user has interacted with the field, it returns [SecurityCodeError.Empty].
     * - If the input is not blank but doesn't match the required length (or range when no cardCode is provided),
     *   it returns [SecurityCodeError.Invalid].
     * - When no cardCode is provided (default validation), accepts 3-4 digits.
     * - Otherwise, it returns [SecurityCodeError.None].
     *
     * @param securityCode The security code input provided by the user.
     * @param cardCode The expected security code configuration for the given card scheme.
     * @param hasUserInteracted A flag indicating whether the user has interacted with the input field.
     * @return A [SecurityCodeError] representing the validation state.
     */
    fun validateSecurityCodeInput(
        securityCode: String,
        cardCode: CardCode?,
        hasUserInteracted: Boolean
    ): SecurityCodeError {
        return when {
            securityCode.isBlank() && hasUserInteracted -> SecurityCodeError.Empty
            securityCode.isNotBlank() -> {
                val isValidLength = if (cardCode != null) {
                    // When cardCode is provided, validate exact length
                    securityCode.length == cardCode.size
                } else {
                    // When no cardCode is provided (default validation), accept 3-4 digits
                    securityCode.length in MobileSDKConstants.CardDetailsConfig.MIN_SECURITY_CODE_LENGTH..MobileSDKConstants.CardDetailsConfig.MAX_SECURITY_CODE_LENGTH
                }
                if (!isValidLength) SecurityCodeError.Invalid else SecurityCodeError.None
            }
            else -> SecurityCodeError.None
        }
    }
}