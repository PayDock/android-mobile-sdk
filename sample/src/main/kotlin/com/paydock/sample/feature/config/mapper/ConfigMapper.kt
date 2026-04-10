package com.paydock.sample.feature.config.mapper

import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType

fun WidgetType.mapWidgetTypeToConfigComponents(): List<ConfigComponent> {
    return when (this) {
        WidgetType.CARD_DETAILS -> listOf(
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.GATEWAY_ID,
            ConfigComponent.COLLECT_CARDHOLDER_NAME,
            ConfigComponent.ALLOW_SAVE_CARD,
            ConfigComponent.STORE_SECURITY_CODE,
            ConfigComponent.SCHEME_SUPPORT
        )

        WidgetType.GIFT_CARD -> listOf(
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.STORE_PIN
        )

        WidgetType.PAY_PAL -> listOf(
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.GATEWAY_ID,
            ConfigComponent.REQUEST_SHIPPING,
            ConfigComponent.FUNDING_SOURCE
        )

        WidgetType.GOOGLE_PAY -> listOf(
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.SERVICE_ID,
            ConfigComponent.EMAIL_REQUIRED,
            ConfigComponent.PHONE_NUMBER_REQUIRED,
            ConfigComponent.BILLING_REQUIRED,
            ConfigComponent.SHIPPING_REQUIRED
        )

        WidgetType.COLES_PAY -> listOf(
            ConfigComponent.CLIENT_ID
        )

        WidgetType.CLICK_TO_PAY -> listOf(
            ConfigComponent.SERVICE_ID,
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.CLICK_TO_PAY_META
        )

        WidgetType.AFTER_PAY -> listOf(
            ConfigComponent.AFTERPAY_LOCALE,
            ConfigComponent.AFTERPAY_CHECKOUT_OPTIONS
        )

        WidgetType.PAY_PAL_VAULT -> listOf(
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.GATEWAY_ID
        )

        WidgetType.ADDRESS_DETAILS -> listOf(
            ConfigComponent.BILLING_ADDRESS
        )

        WidgetType.ZIP -> listOf(
            ConfigComponent.ACCESS_TOKEN,
            ConfigComponent.GATEWAY_ID,
            ConfigComponent.ZIP_FIRST_NAME,
            ConfigComponent.ZIP_LAST_NAME,
            ConfigComponent.ZIP_EMAIL,
            ConfigComponent.ZIP_PHONE,
            ConfigComponent.ZIP_TOKENIZE,
            ConfigComponent.ZIP_GENDER,
            ConfigComponent.ZIP_DATE_OF_BIRTH,
            ConfigComponent.ZIP_SHIPPING_TYPE,
            ConfigComponent.ZIP_BILLING_ADDRESS,
            ConfigComponent.ZIP_SHIPPING_ADDRESS
        )

        WidgetType.MPGS_3DS,
        WidgetType.STANDALONE_3DS -> emptyList()
    }.distinctBy { it.name }
        .sortedBy { it.displayName() }
}



