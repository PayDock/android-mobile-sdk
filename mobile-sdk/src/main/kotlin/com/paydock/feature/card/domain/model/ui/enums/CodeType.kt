package com.paydock.feature.card.domain.model.ui.enums

/**
 * Enum class representing different types of card security codes along with the
 * number of required digits for each code type.
 */
internal enum class CodeType {
    /**
     * Card Verification Value (CVV) security code type.
     * Required digits: 3
     */
    CVV,

    /**
     * Card Verification Code (CVC) security code type.
     * Required digits: 3
     */
    CVC,

    /**
     * Card Identification Number (CID) security code type.
     * Required digits: 3|4
     */
    CID,

    /**
     * Card Verification Number (CVN) security code type.
     * Required digits: 3
     */
    CVN,

    /**
     * Card Verification Value / Card Verification Code (CVV / CVC) security code type.
     * Dual networked cards that can use either CVV or CVC.
     * Required digits: 3
     */
    CVV_CVC
}
