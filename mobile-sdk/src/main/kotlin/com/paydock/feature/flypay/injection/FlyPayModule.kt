package com.paydock.feature.flypay.injection

import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for FlyPay-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val flyPayModule = module {
    viewModelOf(::FlyPayViewModel)
}