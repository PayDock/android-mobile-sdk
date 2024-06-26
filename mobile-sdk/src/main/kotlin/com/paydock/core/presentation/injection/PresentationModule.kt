package com.paydock.core.presentation.injection

import android.content.Context
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.afterpay.injection.afterpayModule
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.flypay.injection.flyPayModule
import com.paydock.feature.googlepay.injection.googlePayModule
import com.paydock.feature.paypal.injection.payPalModule
import com.paydock.feature.src.injection.mastercardSRCModule
import com.paydock.feature.threeDS.injection.threeDSModule
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
    includes(
        cardDetailsModule,
        threeDSModule,
        addressDetailsModule,
        walletModule,
        googlePayModule,
        payPalModule,
        flyPayModule,
        afterpayModule,
        mastercardSRCModule
    )
}