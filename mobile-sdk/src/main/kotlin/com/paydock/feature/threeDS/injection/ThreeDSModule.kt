package com.paydock.feature.threeDS.injection

import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.feature.threeDS.presentation.viewmodels.ThreeDSViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for 3DS-related components including repositories, use cases, and view models.
 */
val threeDSModule = module {
    includes(dispatchersModule)
    // Define a view model for GooglePayViewModel
    viewModelOf(::ThreeDSViewModel)
}