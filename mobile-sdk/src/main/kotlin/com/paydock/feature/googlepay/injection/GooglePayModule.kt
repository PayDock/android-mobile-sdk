package com.paydock.feature.googlepay.injection

import android.content.Context
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.paydock.MobileSDK
import com.paydock.core.domain.model.Environment
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.json.JSONObject
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Google Pay-related components including view models.
 */
internal val googlePayModule = module {
    includes(walletModule)

    // Define a singleton instance of the Google Pay PaymentsClient
    single {
        // Determine the base URL based on the specified environment.
        val environment = when (MobileSDK.getInstance().environment) {
            Environment.PRODUCTION -> WalletConstants.ENVIRONMENT_PRODUCTION
            Environment.SANDBOX, Environment.STAGING -> WalletConstants.ENVIRONMENT_TEST
        }

        // Create Google Pay WalletOptions with the determined environment
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()

        // Create and configure the PaymentsClient
        Wallet.getPaymentsClient(get() as Context, walletOptions)
    }

    // Define a view model for GooglePayViewModel
    viewModel { (isReadyToPayRequest: JSONObject) ->
        GooglePayViewModel(
            get(),
            isReadyToPayRequest,
            get(),
            get(),
            get(),
            get()
        )
    }
}
