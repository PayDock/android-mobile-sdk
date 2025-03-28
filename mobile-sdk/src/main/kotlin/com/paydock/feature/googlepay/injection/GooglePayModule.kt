package com.paydock.feature.googlepay.injection

import android.content.Context
import com.google.android.gms.wallet.Wallet
import com.paydock.MobileSDK
import com.paydock.core.domain.mapper.mapToGooglePayEnv
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.json.JSONObject
import org.koin.core.module.dsl.viewModel
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
