package com.paydock.feature.googlepay.injection

import android.content.Context
import com.google.android.gms.wallet.Wallet
import com.paydock.MobileSDK
import com.paydock.core.domain.mapper.mapToGooglePayEnv
import com.paydock.feature.googlepay.data.repository.GooglePayRepositoryImpl
import com.paydock.feature.googlepay.domain.model.integration.GooglePayWidgetConfig
import com.paydock.feature.googlepay.domain.repository.GooglePayRepository
import com.paydock.feature.googlepay.domain.usecase.CreateGooglePayTokenUseCase
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Google Pay-related components including view models.
 */
internal val googlePayModule = module {
    includes(walletModule)

    // Define a singleton instance of the Google Pay PaymentsClient
    single {
        // Create Google Pay WalletOptions with the determined environment
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(MobileSDK.getInstance().environment.mapToGooglePayEnv())
            .build()

        // Create and configure the PaymentsClient
        Wallet.getPaymentsClient(get() as Context, walletOptions)
    }

    // Provide the repository for managing Google Pay tokens
    single<GooglePayRepository> {
        GooglePayRepositoryImpl(dispatcher = get(named("IO")), client = get())
    }

    // Token Based UseCases
    factoryOf(::CreateGooglePayTokenUseCase)

    // Define a view model for GooglePayViewModel
    // SavedStateHandle is auto-injected by Koin when not in the lambda parameters
    viewModel { (config: GooglePayWidgetConfig) ->
        GooglePayViewModel(
            get(),
            config,
            get(),
            get()
        )
    }
}
