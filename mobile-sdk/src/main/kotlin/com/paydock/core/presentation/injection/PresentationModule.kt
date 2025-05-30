package com.paydock.core.presentation.injection

import android.content.Context
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.afterpay.injection.afterPayModule
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.colespay.injection.colesPayModule
import com.paydock.feature.googlepay.injection.googlePayModule
import com.paydock.feature.paypal.core.injection.payPalModule
import com.paydock.feature.src.injection.clickToPayModule
import com.paydock.feature.threeDS.integrated.injection.integrated3DSModule
import com.paydock.feature.threeDS.standalone.injection.standalone3DSModule
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * The Koin module for defining application-level dependencies.
 */
internal val presentationModule = module {
    // Define the appContext dependency
    single<Context> { androidApplication() }
    // Define the MobileSDKKoinContext dependency
    single { MobileSDKKoinContext(get()) }

    // Feature modules
    includes(
        cardDetailsModule,
        integrated3DSModule,
        standalone3DSModule,
        addressDetailsModule,
        clickToPayModule,
        // wallet modules
        googlePayModule,
        payPalModule,
        colesPayModule,
        afterPayModule
    )
}