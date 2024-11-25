package com.paydock.sample.feature.widgets.ui.models

import com.paydock.sample.designsystems.components.list.DisplayableListItem

enum class WidgetType : DisplayableListItem {
    ADDRESS_DETAILS, AFTER_PAY, CARD_DETAILS, FLY_PAY, GOOGLE_PAY, GIFT_CARD, INTEGRATED_3DS, CLICK_TO_PAY, PAY_PAL, PAY_PAL_VAULT, STANDALONE_3DS;

    override fun displayName(): String = when (this) {
        ADDRESS_DETAILS -> "Address"
        AFTER_PAY -> "Afterpay"
        CLICK_TO_PAY -> "Click to Pay"
        CARD_DETAILS -> "Card Details"
        FLY_PAY -> "FlyPay"
        GIFT_CARD -> "Gift Card"
        GOOGLE_PAY -> "Google Pay"
        INTEGRATED_3DS -> "Integrated 3DS"
        PAY_PAL -> "PayPal"
        PAY_PAL_VAULT -> "PayPal Vault"
        STANDALONE_3DS -> "Standalone 3DS"
    }

    override fun displayDescription(): String = when (this) {
        ADDRESS_DETAILS -> "Capture customer address form"
        AFTER_PAY -> "Standalone Afterpay button"
        CLICK_TO_PAY -> "ClickToPay flow"
        CARD_DETAILS -> "Tokensise card details"
        FLY_PAY -> "Standalone FlyPay button"
        GIFT_CARD -> "Tokensise card details"
        GOOGLE_PAY -> "Standalone Google Pay button"
        INTEGRATED_3DS -> "Integrated 3DS flow"
        PAY_PAL -> "Standalone PayPal button"
        PAY_PAL_VAULT -> "Link your Paypal account for faster checkout"
        STANDALONE_3DS -> "Standalone 3DS flow"
    }
}