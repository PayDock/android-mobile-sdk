/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
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

package com.paydock.feature.googlepay.injection

import android.content.Context
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.paydock.MobileSDK
import com.paydock.core.data.injection.modules.dispatchersModule
import com.paydock.core.domain.model.Environment
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import com.paydock.feature.wallet.injection.walletModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for Google Pay-related components including view models.
 */
val googlePayModule = module {
    includes(dispatchersModule, walletModule)

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
    viewModelOf(::GooglePayViewModel)
}
