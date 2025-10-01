package com.paydock.feature.paypal.vault.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalVaultViewModel
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalWebVaultViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for PayPal Vault-related components including repositories, use cases.
 */
internal val payPalVaultModule = module {
    includes(dispatchersModule)

    // Factory methods for creating instances of ViewModels
    // SavedStateHandle is auto-injected by Koin when not in the lambda parameters
    viewModel { (config: PayPalVaultConfig) ->
        PayPalVaultViewModel(config, get(), get(), get(), get(), get())
    }

    // PayPalWebVaultViewModel with SavedStateHandle
    viewModel {
        PayPalWebVaultViewModel(
            savedStateHandle = get(),
            dispatchers = get()
        )
    }
}