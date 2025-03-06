package com.paydock.core

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.util.TestDispatchersProviderImpl
import org.junit.Before
import org.junit.Rule
import org.koin.dsl.module
import org.koin.test.inject

internal abstract class BaseViewModelKoinTest<T : ViewModel> : BaseUITest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    protected lateinit var viewModel: T

    protected val instrumentedTestModule = module {
        // Add any common injection
        single<DispatchersProvider> { TestDispatchersProviderImpl }
    }

    protected val dispatchersProvider: DispatchersProvider by inject()

    @Before
    open fun onStart() {
        viewModel = initialiseViewModel()
    }

    abstract fun initialiseViewModel(): T
}