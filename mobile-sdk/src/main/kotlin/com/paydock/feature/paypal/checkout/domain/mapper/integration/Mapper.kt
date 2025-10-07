package com.paydock.feature.paypal.checkout.domain.mapper.integration

import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource

fun PayPalWidgetConfig.PayPalFundingSource.mapToPayPalFundingSource(): PayPalWebCheckoutFundingSource =
    when (this) {
        PayPalWidgetConfig.PayPalFundingSource.PAYPAL -> PayPalWebCheckoutFundingSource.PAYPAL
        PayPalWidgetConfig.PayPalFundingSource.PAY_LATER -> PayPalWebCheckoutFundingSource.PAY_LATER
        PayPalWidgetConfig.PayPalFundingSource.PAYPAL_CREDIT -> PayPalWebCheckoutFundingSource.PAYPAL_CREDIT
    }