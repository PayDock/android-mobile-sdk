package com.paydock.feature.paypal.checkout.injection

import com.paydock.feature.paypal.checkout.presentation.viewmodels.PayPalViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for PayPal Checkout-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val payPalCheckoutModule = module {
    // Define a view model for PayPalViewModel
    viewModelOf(::PayPalViewModel)
}