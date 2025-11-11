package com.paydock.feature.paypal.checkout.injection

import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalViewModel
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalWebCheckoutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for PayPal Checkout-related components.
 *
 * Simplified with [PayPalWebClientManager] handling client lifecycle management.
 */
internal val payPalCheckoutModule = module {
    // Define a view model for PayPalViewModel with SavedStateHandle
    // SavedStateHandle is auto-injected by Koin when not in the lambda parameters
    viewModel { (config: PayPalWidgetConfig) ->
        PayPalViewModel(config, get(), get(), get(), get(), get(), get())
    }

    // PayPalWebCheckoutViewModel - uses default PayPalWebClientManager
    // SavedStateHandle and dispatchers are auto-injected by Koin
    viewModel {
        PayPalWebCheckoutViewModel(
            savedStateHandle = get(),
            dispatchers = get()
        )
    }
}