package com.paydock.feature.paypal.vault.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalVaultViewModel
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalWebVaultViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for PayPal Vault-related components including repositories, use cases.
 */
internal val payPalVaultModule = module {
    includes(dispatchersModule)

    // Factory methods for creating instances of ViewModels

    viewModel { (config: PayPalVaultConfig) ->
        PayPalVaultViewModel(config, get(), get(), get(), get())
    }

    viewModel {
        PayPalWebVaultViewModel(get())
    }
}