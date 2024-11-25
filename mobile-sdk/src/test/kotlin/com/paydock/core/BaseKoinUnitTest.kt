package com.paydock.core

import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.injection.sdkModule
import com.paydock.core.injection.testSdkModule
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

abstract class BaseKoinUnitTest : BaseUnitTest() {

    @Before
    fun setupKoin() {
        // Load the Koin modules here, if needed for the test
        startKoin {
            modules(
                // Includes basic architecture modules (data, domain and presentation)
                sdkModule,
                // Includes core testing module (context)
                testSdkModule,
                // Includes core networking module (can be substituted)
                mockSuccessNetworkModule
            )
        }
    }

    @After
    fun tearDownKoin() {
        stopKoin()
    }
}