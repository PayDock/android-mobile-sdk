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
     * - If the input is not blank but incomplete (doesn't match the required length), it returns [SecurityCodeError.Invalid].
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
            securityCode.isNotBlank() && securityCode.length != (
                cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
                ) -> SecurityCodeError.Invalid
            else -> SecurityCodeError.None
        }
    }
}