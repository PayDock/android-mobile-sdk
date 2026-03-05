package com.paydock.core

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.paydock.MobileSDK
import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.core.domain.model.Environment
import com.paydock.core.injection.sdkModule
import com.paydock.initializeMobileSDK
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito

internal abstract class BaseUITest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Before
    fun setUpMocks() {
        // Reset from previous test or app init - app may have already started Koin with production modules
        try {
            stopKoin()
        } catch (_: Exception) { /* Koin not started */ }
        MobileSDK.reset()

        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        // Start Koin with mock modules BEFORE initializeMobileSDK so that BinDataRefreshCoordinator
        // and BinDataCacheManager resolve with mocked dependencies (no real CloudFront requests).
        // MobileSDKKoinContext detects existing Koin and uses it instead of starting a new one.
        startKoin {
            allowOverride(true)
            androidContext(application)
            modules(
                sdkModule,
                InstrumentationTestKoinModules.mockNetworkModule,
                InstrumentationTestKoinModules.mockBinDataSuccessModule,
                module {
                    single<BinDataRefreshCoordinator> { mockk(relaxed = true) }
                }
            )
        }
        application.initializeMobileSDK(Environment.SANDBOX)
    }

    @After
    fun tearDownMocks() {
        clearAllMocks()
        unmockkAll()
        MobileSDK.reset() // Reset MobileSDK before each test
    }

    @After
    open fun tearDownKoin() {
        // As the SDK will startKoin, we need to ensure that after each test we stop koin to be able to restart it in each test.
        // Guard against double-stop which can leave Koin in a bad state for subsequent tests.
        try {
            stopKoin()
        } catch (_: Exception) { /* Koin already stopped or not started */ }
    }

    protected fun getStringRes(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

}