package com.paydock.core.data.injection.modules

import com.paydock.core.domain.injection.domainModule
import com.paydock.core.presentation.injection.presentationModule
import org.koin.dsl.module

/**
 * Injection module responsible for handling global SDK modules. It will contain various modules contained within SDK.
 */
val sdkModule = module {
    includes(
        presentationModule,
        domainModule,
        dataModule
    )
}