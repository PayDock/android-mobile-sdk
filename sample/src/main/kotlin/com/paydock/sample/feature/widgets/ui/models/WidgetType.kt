package com.paydock.sample.feature.widgets.ui.models

enum class WidgetType {
    ADDRESS_DETAILS, AFTER_PAY, CREDIT_CARD_DETAILS, FLY_PAY, GOOGLE_PAY, GIFT_CARD_DETAILS, INTEGRATED_3DS, MASTERCARD_SRC, PAY_PAL, STANDALONE_3DS,
}

fun WidgetType.displayName(): String = when (this) {
    WidgetType.ADDRESS_DETAILS -> "Address"
    WidgetType.AFTER_PAY -> "Afterpay"
    WidgetType.CREDIT_CARD_DETAILS -> "Credit Card Details"
    WidgetType.FLY_PAY -> "FlyPay"
    WidgetType.GIFT_CARD_DETAILS -> "Gift Card Details"
    WidgetType.GOOGLE_PAY -> "Google Pay"
    WidgetType.INTEGRATED_3DS -> "Integrated 3DS"
    WidgetType.MASTERCARD_SRC -> "Mastercard SRC"
    WidgetType.PAY_PAL -> "PayPal"
    WidgetType.STANDALONE_3DS -> "Standalone 3DS"
}