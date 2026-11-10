package com.paydock.core.injection

import com.paydock.core.data.injection.modules.dataModule
import com.paydock.core.domain.injection.domainModule
import com.paydock.core.presentation.injection.presentationModule
import com.paydock.core.telemetry.defaultTelemetryModule
import org.koin.dsl.module

/**
 * Injection module responsible for handling global SDK modules. It will contain various modules contained within SDK.
 * It includes the default no-op telemetry facade, which the telemetry binding at SDK start overrides.
 */
internal val sdkModule = module {
    includes(
        presentationModule,
        domainModule,
        dataModule,
        defaultTelemetryModule
    )
}