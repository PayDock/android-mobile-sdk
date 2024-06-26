package com.paydock.feature.card.presentation.model

import com.paydock.core.MobileSDKConstants

/**
 * Enum class representing different types of card security codes along with the
 * number of required digits for each code type.
 *
 * @property requiredDigits The number of digits required for the security code type.
 */
enum class SecurityCodeType(val requiredDigits: Int) {
    /**
     * Card Verification Value (CVV) security code type.
     * Required digits: 3
     */
    CVV(MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH),

    /**
     * Card Verification Code (CVC) security code type.
     * Required digits: 3
     */
    CVC(MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH),

    /**
     * Card Security Code (CSC) security code type.
     * Required digits: 4
     */
    CSC(MobileSDKConstants.CardDetailsConfig.CSC_LENGTH)
}
