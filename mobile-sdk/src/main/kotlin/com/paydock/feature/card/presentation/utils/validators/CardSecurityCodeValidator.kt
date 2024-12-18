package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.enums.CardIssuerType
import com.paydock.feature.card.domain.model.integration.enums.SecurityCodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError

/**
 * A utility object for validating and detecting credit card security codes (CVV, CVC, CSC) based on card issuer and security code type.
 */
internal object CardSecurityCodeValidator {

    /**
     * Validates whether the given security code is valid for the specified security code type.
     *
     * A valid security code:
     * - Is not blank.
     * - Contains only numeric digits.
     * - Has a length that is less than or equal to the required length for the given [securityCodeType].
     *
     * @param code The security code to validate.
     * @param securityCodeType The type of security code (e.g., CVV, CVC, CSC) which defines the required number of digits.
     * @return `true` if the security code is valid; otherwise, `false`.
     */
    fun isSecurityCodeValid(code: String, securityCodeType: SecurityCodeType): Boolean {
        return code.isNotBlank() && code.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) && code.length <= securityCodeType.requiredDigits
    }

    /**
     * Checks if the provided security code is complete.
     *
     * A security code is considered complete if:
     * - It contains only numeric digits.
     * - Its length matches exactly the required digits defined by the [securityCodeType].
     *
     * @param code The security code to check.
     * @param securityCodeType The type of security code (e.g., CVV, CVC, CSC).
     * @return `true` if the security code is complete; otherwise, `false`.
     */
    fun isSecurityCodeComplete(code: String, securityCodeType: SecurityCodeType): Boolean {
        return code.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) && code.length == securityCodeType.requiredDigits
    }

    /**
     * Detects the type of security code (CVV, CVC, CSC) based on the given card issuer type.
     *
     * - For AMERICAN_EXPRESS and DINERS_CLUB, it returns [SecurityCodeType.CSC].
     * - For MASTERCARD, it returns [SecurityCodeType.CVC].
     * - For all other issuers (e.g., VISA, DISCOVER, UNION_PAY), it defaults to [SecurityCodeType.CVV].
     *
     * @param cardIssuer The issuer type of the card.
     * @return The corresponding [SecurityCodeType] for the card issuer.
     */
    fun detectSecurityCodeType(cardIssuer: CardIssuerType): SecurityCodeType = when (cardIssuer) {
        CardIssuerType.AMERICAN_EXPRESS, CardIssuerType.DINERS_CLUB -> SecurityCodeType.CSC
        CardIssuerType.MASTERCARD -> SecurityCodeType.CVC
        else -> SecurityCodeType.CVV // Default to CVV for VISA, DISCOVER, UNION_PAY, and others
    }

    /**
     * Validates the user input for a security code.
     *
     * The validation considers the following cases:
     * - If the input is blank and the user has interacted with the field, it returns [SecurityCodeError.Empty].
     * - If the input is not blank but incomplete (doesn't match the required length), it returns [SecurityCodeError.Invalid].
     * - Otherwise, it returns [SecurityCodeError.None].
     *
     * @param securityCode The security code input provided by the user.
     * @param securityCodeType The type of security code (e.g., CVV, CVC, CSC).
     * @param hasUserInteracted A flag indicating whether the user has interacted with the input field.
     * @return A [SecurityCodeError] representing the validation state.
     */
    fun validateSecurityCodeInput(
        securityCode: String,
        securityCodeType: SecurityCodeType,
        hasUserInteracted: Boolean
    ): SecurityCodeError {
        return when {
            securityCode.isBlank() && hasUserInteracted -> SecurityCodeError.Empty
            securityCode.isNotBlank() && !isSecurityCodeComplete(securityCode, securityCodeType) -> SecurityCodeError.Invalid
            else -> SecurityCodeError.None
        }
    }
}