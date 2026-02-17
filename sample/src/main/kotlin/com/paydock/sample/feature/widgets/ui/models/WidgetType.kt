package com.paydock.sample.feature.widgets.ui.models

import androidx.compose.runtime.Composable
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.feature.address.presentation.AddressDetailsAppearanceDefaults
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.DisplayableListItem

enum class WidgetType : DisplayableListItem {
    ADDRESS_DETAILS, AFTER_PAY, CARD_DETAILS, COLES_PAY, GOOGLE_PAY, GIFT_CARD, MPGS_3DS, CLICK_TO_PAY, PAY_PAL, PAY_PAL_VAULT, STANDALONE_3DS, ZIP;

    override fun displayIcon(): Int? = when (this) {
        ADDRESS_DETAILS -> R.drawable.ic_address_widget
        AFTER_PAY -> R.drawable.ic_afterpay_widget
        CLICK_TO_PAY -> R.drawable.ic_click_to_pay_widget
        CARD_DETAILS -> R.drawable.ic_card_widget
        COLES_PAY -> R.drawable.ic_coles_pay_widget
        GIFT_CARD -> R.drawable.ic_gift_card_widget
        GOOGLE_PAY -> R.drawable.ic_google_widget
        MPGS_3DS -> R.drawable.ic_integrated_3ds_widget
        PAY_PAL -> R.drawable.ic_paypal_widget
        PAY_PAL_VAULT -> R.drawable.ic_paypal_widget
        STANDALONE_3DS -> R.drawable.ic_standalone_3ds_widget
        ZIP -> R.drawable.ic_zip_widget
    }

    override fun displayName(): String = when (this) {
        ADDRESS_DETAILS -> "Address"
        AFTER_PAY -> "Afterpay"
        CLICK_TO_PAY -> "Click to Pay"
        CARD_DETAILS -> "Card Details"
        COLES_PAY -> "Coles Pay"
        GIFT_CARD -> "Gift Card"
        GOOGLE_PAY -> "Google Pay"
        MPGS_3DS -> "MPGS 3DS"
        PAY_PAL -> "PayPal"
        PAY_PAL_VAULT -> "PayPal Vault"
        STANDALONE_3DS -> "Standalone 3DS"
        ZIP -> "Zip"
    }

    override fun displayDescription(): String = when (this) {
        ADDRESS_DETAILS -> "Capture customer address form"
        AFTER_PAY -> "Standalone Afterpay button"
        CLICK_TO_PAY -> "ClickToPay flow"
        CARD_DETAILS -> "Tokensise card details"
        COLES_PAY -> "Standalone Coles Pay button"
        GIFT_CARD -> "Tokensise card details"
        GOOGLE_PAY -> "Standalone Google Pay button"
        MPGS_3DS -> "MPGS Integrated 3DS widget"
        PAY_PAL -> "Standalone PayPal button"
        PAY_PAL_VAULT -> "Link your Paypal account for faster checkout"
        STANDALONE_3DS -> "Standalone 3DS flow"
        ZIP -> "Standalone Zip button"
    }
}

@Composable
fun WidgetType.toTitleAppearance(): TextAppearance {
    return when (this) {
        WidgetType.ADDRESS_DETAILS -> AddressDetailsAppearanceDefaults.appearance().title
        else -> TextAppearanceDefaults.appearance()
    }
}
