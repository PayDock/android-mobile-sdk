package com.paydock.core.domain.injection

import com.paydock.core.data.injection.modules.dataModule
import org.koin.dsl.module

/**
 * Injection module responsible for handling Domain layer. It will contain our repositories as well as our use cases.
 */
internal val domainModule = module {
    includes(dataModule)
}
