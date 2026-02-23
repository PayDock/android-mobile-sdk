package com.paydock.core

import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.core.data.injection.modules.mockBinDataSuccessModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.injection.sdkModule
import com.paydock.core.injection.testSdkModule
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

abstract class BaseKoinUnitTest : BaseUnitTest() {

    @Before
    fun setupKoin() {
        // Load the Koin modules here, if needed for the test
        startKoin {
            allowOverride(true)
            modules(
                // Includes basic architecture modules (data, domain and presentation)
                sdkModule,
                // Includes core testing module (context)
                testSdkModule,
                // Includes core networking module (can be substituted)
                mockSuccessNetworkModule,
                // Mock HTTP client for BIN data (CloudFront)
                mockBinDataSuccessModule,
                // Override BinDataRefreshCoordinator with a mock to avoid ProcessLifecycleOwner issues
                testBinDataRefreshCoordinatorModule
            )
        }
    }

    companion object {
        /**
         * Test module that provides a mock BinDataRefreshCoordinator.
         * This avoids the ProcessLifecycleOwner.get() call in the real constructor.
         */
        private val testBinDataRefreshCoordinatorModule = module {
            single<BinDataRefreshCoordinator> {
                mockk(relaxed = true)
            }
        }
    }

    @After
    fun tearDownKoin() {
        stopKoin()
    }
}