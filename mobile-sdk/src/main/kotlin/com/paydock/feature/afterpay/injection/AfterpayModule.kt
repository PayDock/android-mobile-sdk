package com.paydock.feature.afterpay.injection

import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Afterpay-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val afterPayModule = module {
    // Define a view model for Afterpay
    viewModelOf(::AfterpayViewModel)
}