package com.paydock.feature.paypal.injection

import com.paydock.feature.paypal.checkout.injection.payPalCheckoutModule
import com.paydock.feature.paypal.vault.injection.payPalVaultModule
import org.koin.dsl.module

/**
 * Koin module for PayPal-related components including payPalCheckoutModule and payPalVaultModule.
 */
internal val payPalModule = module {
    includes(payPalCheckoutModule, payPalVaultModule)
}