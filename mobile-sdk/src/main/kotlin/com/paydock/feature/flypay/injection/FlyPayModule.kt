package com.paydock.feature.flypay.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for FlyPay-related components including repositories, use cases, and view models as well as walletModule.
 */
val flyPayModule = module {
    includes(dispatchersModule, walletModule)
    viewModelOf(::FlyPayViewModel)
}