package com.paydock.core.data.injection.modules

import com.paydock.api.charges.data.repository.ChargeRepositoryImpl
import com.paydock.api.charges.domain.repository.ChargeRepository
import com.paydock.api.gateways.data.repository.GatewayRepositoryImpl
import com.paydock.api.gateways.domain.repository.GatewayRepository
import com.paydock.api.tokens.data.repository.TokenRepositoryImpl
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.data.util.ConnectivityManagerNetworkMonitor
import com.paydock.core.data.util.NetworkMonitor
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Injection module responsible for handling Data layer. It will contain our data sources and networking layer.
 */
internal val dataModule = module {
    includes(dispatchersModule, networkModule)
    singleOf(::ConnectivityManagerNetworkMonitor) { bind<NetworkMonitor>() }

    // Provide the repository for managing tokens
    single<TokenRepository> {
        TokenRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }

    // Provide the repository for managing charges
    single<ChargeRepository> {
        ChargeRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }

    // Provide the repository for managing charges
    single<GatewayRepository> {
        GatewayRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }
}
