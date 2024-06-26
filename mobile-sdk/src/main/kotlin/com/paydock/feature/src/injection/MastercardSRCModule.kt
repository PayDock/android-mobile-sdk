package com.paydock.feature.src.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.src.presentation.viewmodels.MastercardSRCViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Mastercard SRC-related components including repositories, use cases, and view models.
 */
val mastercardSRCModule = module {
    includes(dispatchersModule)
    // Define a view model for GooglePayViewModel
    viewModelOf(::MastercardSRCViewModel)
}