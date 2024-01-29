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