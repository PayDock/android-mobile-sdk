package com.paydock.core

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.util.TestDispatchersProviderImpl
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito

abstract class BaseViewModelKoinTest<T : ViewModel> : KoinTest {

    protected lateinit var viewModel: T

    @get:Rule
    val composeTestRule = createComposeRule()

    protected val instrumentedTestModule = module {
        // Add any common injection
        single<DispatchersProvider> { TestDispatchersProviderImpl }
    }

    protected val dispatchersProvider: DispatchersProvider by inject()

    @get:Rule
    abstract val koinTestRule: KoinTestRule

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Before
    open fun onStart() {
        viewModel = initialiseViewModel()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    abstract fun initialiseViewModel(): T
}