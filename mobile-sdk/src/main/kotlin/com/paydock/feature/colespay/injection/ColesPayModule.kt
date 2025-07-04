package com.paydock.feature.colespay.injection

import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.viewmodels.ColesPayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Coles Pay -related components including repositories, use cases, and view models as well as walletModule.
 */
internal val colesPayModule = module {
    includes(walletModule)

    viewModel { (config: ColesPayWidgetConfig) ->
        ColesPayViewModel(config, get(), get(), get(), get())
    }
}