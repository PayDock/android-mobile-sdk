package com.paydock.feature.wallet.injection

import com.paydock.core.data.injection.modules.dataModule
import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.core.domain.injection.domainModule
import com.paydock.feature.wallet.data.repository.WalletRepositoryImpl
import com.paydock.feature.wallet.domain.repository.WalletRepository
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Wallet-related components including repositories, use cases, and view models.
 */
val walletModule = module {
    // Include other necessary modules
    includes(dispatchersModule, dataModule, domainModule)

    // Provide the repository for managing wallet-related data
    single<WalletRepository> {
        WalletRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }

    // Factory methods for creating instances of UseCases
    factoryOf(::CaptureWalletTransactionUseCase)
    factoryOf(::DeclineWalletTransactionUseCase)
    factoryOf(::GetWalletCallbackUseCase)
}