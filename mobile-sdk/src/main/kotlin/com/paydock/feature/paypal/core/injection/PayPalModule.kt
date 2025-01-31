package com.paydock.feature.paypal.core.injection

import com.paydock.feature.paypal.checkout.injection.payPalCheckoutModule
import com.paydock.feature.paypal.core.data.repository.PayPalRepositoryImpl
import com.paydock.feature.paypal.core.domain.repository.PayPalRepository
import com.paydock.feature.paypal.core.domain.usecase.CreatePayPalVaultPaymentTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.CreateSetupTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.vault.injection.payPalVaultModule
import com.paydock.feature.wallet.injection.walletModule
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for PayPal-related components including payPalCheckoutModule and payPalVaultModule.
 */
internal val payPalModule = module {
    includes(walletModule, payPalCheckoutModule, payPalVaultModule)

    // Provide the repository for managing tokens
    single<PayPalRepository> {
        PayPalRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }

    // Token Based UseCases
    factoryOf(::CreateSetupTokenUseCase)
    factoryOf(::CreatePayPalVaultPaymentTokenUseCase)

    // Gateway Based UseCases
    factoryOf(::GetPayPalClientIdUseCase)
}