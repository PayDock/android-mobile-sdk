/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:06 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.wallet.injection

import com.paydock.core.data.injection.modules.dataModule
import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.core.data.injection.modules.provideHttpClient
import com.paydock.core.data.injection.modules.provideHttpEngine
import com.paydock.core.domain.injection.domainModule
import com.paydock.feature.wallet.data.repository.WalletRepositoryImpl
import com.paydock.feature.wallet.domain.repository.WalletRepository
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
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

    // Provide an HTTP engine for wallet operations
    single(named("WalletEngine")) {
        provideHttpEngine(get(), null, get(), get())
    }

    // Provide an HTTP client for wallet operations using the provided engine
    single(named("WalletClient")) {
        provideHttpClient(get(), get(named("WalletEngine")))
    }

    // Provide the repository for managing wallet-related data
    single<WalletRepository> {
        WalletRepositoryImpl(dispatcher = get(named("IO")), client = get(named("WalletClient")))
    }

    // Factory methods for creating instances of UseCases
    factoryOf(::CaptureWalletTransactionUseCase)
    factoryOf(::GetWalletCallbackUseCase)
}