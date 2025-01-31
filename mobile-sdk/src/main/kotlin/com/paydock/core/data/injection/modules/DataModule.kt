package com.paydock.core.data.injection.modules

import com.paydock.core.data.util.ConnectivityManagerNetworkMonitor
import com.paydock.core.data.util.NetworkMonitor
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Injection module responsible for handling Data layer. It will contain our data sources and networking layer.
 */
internal val dataModule = module {
    includes(dispatchersModule, networkModule)
    singleOf(::ConnectivityManagerNetworkMonitor) { bind<NetworkMonitor>() }
}
