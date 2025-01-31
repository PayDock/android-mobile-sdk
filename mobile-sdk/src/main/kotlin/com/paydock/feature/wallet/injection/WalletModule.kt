package com.paydock.feature.wallet.injection

import com.paydock.feature.wallet.data.repository.WalletRepositoryImpl
import com.paydock.feature.wallet.domain.repository.WalletRepository
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Injection module responsible for handling Wallet layer. It will contain our repositories as well as our use cases.
 */
internal val walletModule = module {

    // Provide the repository for managing charges
    single<WalletRepository> {
        WalletRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }

    // Charge Based UseCases
    factoryOf(::CaptureWalletChargeUseCase)
    factoryOf(::DeclineWalletChargeUseCase)
    factoryOf(::GetWalletCallbackUseCase)
}
