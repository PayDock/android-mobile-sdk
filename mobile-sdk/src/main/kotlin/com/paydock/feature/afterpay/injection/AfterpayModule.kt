package com.paydock.feature.afterpay.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Afterpay-related components including repositories, use cases, and view models as well as walletModule.
 */
val afterPayModule = module {
    includes(dispatchersModule, walletModule)
    // Define a view model for Afterpay
    viewModelOf(::AfterpayViewModel)
}