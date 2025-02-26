package com.paydock.feature.afterpay.injection

import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Afterpay-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val afterPayModule = module {
    includes(walletModule)

    // Define a view model for Afterpay
    viewModelOf(::AfterpayViewModel)
}