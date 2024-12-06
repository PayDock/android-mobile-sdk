package com.paydock.core.domain.injection

import com.paydock.api.charges.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.GetWalletCallbackUseCase
import com.paydock.api.gateways.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.api.tokens.domain.usecase.CreateCardPaymentTokenFlowUseCase
import com.paydock.api.tokens.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.api.tokens.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.api.tokens.domain.usecase.CreatePayPalVaultPaymentTokenUseCase
import com.paydock.api.tokens.domain.usecase.CreateSetupTokenUseCase
import com.paydock.core.data.injection.modules.dataModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Injection module responsible for handling Domain layer. It will contain our repositories as well as our use cases.
 */
internal val domainModule = module {
    includes(dataModule)

    // Charge Based UseCases
    factoryOf(::CaptureWalletChargeUseCase)
    factoryOf(::DeclineWalletChargeUseCase)
    factoryOf(::GetWalletCallbackUseCase)

    // Token Based UseCases
    factoryOf(::CreateCardPaymentTokenUseCase)
    factoryOf(::CreateCardPaymentTokenFlowUseCase)
    factoryOf(::CreateGiftCardPaymentTokenUseCase)
    factoryOf(::CreateSetupTokenUseCase)
    factoryOf(::CreatePayPalVaultPaymentTokenUseCase)

    // Gateway Based UseCases
    factoryOf(::GetPayPalClientIdUseCase)
}
