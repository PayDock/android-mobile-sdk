package com.paydock.core

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.paydock.MobileSDK
import com.paydock.core.domain.model.Environment
import com.paydock.initializeMobileSDK
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito

internal abstract class BaseUITest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    private lateinit var context: Context

    @Before
    fun setUpMocks() {
        // Mock the Context object
        context = mockk()
        // Configure the getApplicationContext() method to return the mock Context
        every { context.applicationContext } returns context

        val environment = Environment.SANDBOX

        context.initializeMobileSDK(environment)
    }

    @After
    fun tearDownMocks() {
        clearAllMocks()
        unmockkAll()
        MobileSDK.reset() // Reset MobileSDK before each test
    }

    @After
    open fun tearDownKoin() {
        // As the SDK will startKoin, we need to ensure that after each test we stop koin to be able to restart it in each test
        stopKoin()
    }

    protected fun getStringRes(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

}