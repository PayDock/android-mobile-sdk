package com.paydock.feature.paypal.checkout.injection

import androidx.appcompat.app.AppCompatActivity
import com.paydock.MobileSDK
import com.paydock.core.domain.mapper.mapToPayPalEnv
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalWebCheckoutViewModel
import com.paydock.feature.paypal.checkout.presentation.viewmodels.PayPalViewModel
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for PayPal Checkout-related components including repositories, use cases, and view models as well as walletModule.
 */
internal val payPalCheckoutModule = module {
    // Define a view model for PayPalViewModel with SavedStateHandle
    // SavedStateHandle is auto-injected by Koin when not in the lambda parameters
    viewModel { (config: PayPalWidgetConfig) ->
        PayPalViewModel(config, get(), get(), get(), get(), get(), get())
    }

    // PayPalWebCheckoutViewModel with function parameters and SavedStateHandle
    viewModel {
        PayPalWebCheckoutViewModel(
            savedStateHandle = get(),
            dispatchers = get(),
            coreConfigProvider = { clientId ->
                CoreConfig(
                    clientId = clientId,
                    environment = MobileSDK.getInstance().environment.mapToPayPalEnv()
                )
            },
            clientProvider = { activity: AppCompatActivity, coreConfig: CoreConfig, returnUrl: String ->
                PayPalWebCheckoutClient(activity, coreConfig, returnUrl)
            }
        )
    }
}