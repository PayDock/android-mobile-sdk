package com.paydock.feature.src.injection

import com.paydock.feature.src.presentation.viewmodels.ClickToPayViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for ClickToPay-related components including repositories, use cases, and view models.
 */
internal val clickToPayModule = module {
    // Define a view model for GooglePayViewModel
    viewModelOf(::ClickToPayViewModel)
}