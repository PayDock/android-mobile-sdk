package com.paydock.feature.threeDS.standalone.injection

import com.paydock.feature.threeDS.standalone.presentation.viewmodels.Standalone3DSViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Standalone 3DS-related components including repositories, use cases, and view models.
 */
internal val standalone3DSModule = module {
    viewModelOf(::Standalone3DSViewModel)
}