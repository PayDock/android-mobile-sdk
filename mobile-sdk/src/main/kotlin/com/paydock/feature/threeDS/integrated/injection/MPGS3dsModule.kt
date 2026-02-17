package com.paydock.feature.threeDS.integrated.injection

import com.paydock.feature.threeDS.integrated.presentation.viewmodels.MPGS3dsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for MPGS 3DS-related components including repositories, use cases, and view models.
 */
internal val mpgs3dsModule = module {
    viewModelOf(::MPGS3dsViewModel)
}