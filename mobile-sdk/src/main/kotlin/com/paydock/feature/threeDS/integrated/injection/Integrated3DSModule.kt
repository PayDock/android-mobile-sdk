package com.paydock.feature.threeDS.integrated.injection

import com.paydock.feature.threeDS.integrated.presentation.viewmodels.Integrated3DSViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Integrated 3DS-related components including repositories, use cases, and view models.
 */
internal val integrated3DSModule = module {
    viewModelOf(::Integrated3DSViewModel)
}