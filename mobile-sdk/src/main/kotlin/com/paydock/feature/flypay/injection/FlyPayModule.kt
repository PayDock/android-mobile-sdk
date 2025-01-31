package com.paydock.feature.flypay.injection

import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for FlyPay-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val flyPayModule = module {
    includes(walletModule)

    viewModel { (clientId: String) ->
        FlyPayViewModel(clientId, get(), get(), get(), get())
    }
}