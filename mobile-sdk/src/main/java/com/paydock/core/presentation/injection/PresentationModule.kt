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

package com.paydock.core.presentation.injection

import android.content.Context
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.flypay.injection.flyPayModule
import com.paydock.feature.googlepay.injection.googlePayModule
import com.paydock.feature.paypal.injection.payPalModule
import com.paydock.feature.wallet.injection.walletModule
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * The Koin module for defining application-level dependencies.
 */
val presentationModule = module {
    // Define the appContext dependency
    single<Context> { androidApplication() }
    // Define the MobileSDKKoinContext dependency
    single { MobileSDKKoinContext(get()) }

    // Feature modules
    includes(cardDetailsModule, addressDetailsModule, walletModule, googlePayModule, payPalModule, flyPayModule)
}