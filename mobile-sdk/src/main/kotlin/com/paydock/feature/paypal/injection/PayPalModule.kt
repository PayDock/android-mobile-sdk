package com.paydock.feature.paypal.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.paypal.presentation.viewmodels.PayPalViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for PayPal-related components including repositories, use cases, and view models as well as walletModule.
 */
val payPalModule = module {
    includes(dispatchersModule, walletModule)
    // Define a view model for PayPalViewModel
    viewModelOf(::PayPalViewModel)
}