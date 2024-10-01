package com.paydock.core.data.injection.modules

import com.paydock.BuildConfig
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.NetworkClientBuilder
import org.koin.dsl.module

/**
 * Network-based module for handling network components using Ktor.
 *
 * This module provides singleton and factory components for network operations.
 * - Singleton components are kept unique within the Koin container.
 * - Factory components provide a new instance each time they are requested.
 */
val networkModule = module {
    includes(dispatchersModule)
    single {
        NetworkClientBuilder.create()
            .setBaseUrl(MobileSDK.getInstance().baseUrl)
            .apply {
                if (!MobileSDK.getInstance().enableTestMode) {
                    setSslPins(listOf(MobileSDKConstants.Network.SSH_HASH))
                }
            }
            .setDebug(BuildConfig.DEBUG)
            .build()
    }
}
