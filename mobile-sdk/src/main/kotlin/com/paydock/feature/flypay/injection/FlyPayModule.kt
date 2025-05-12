package com.paydock.feature.flypay.injection

import com.paydock.feature.flypay.domain.model.integration.FlyPayWidgetConfig
import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for FlyPay-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val flyPayModule = module {
    includes(walletModule)

    viewModel { (config: FlyPayWidgetConfig) ->
        FlyPayViewModel(config, get(), get(), get(), get())
    }
}