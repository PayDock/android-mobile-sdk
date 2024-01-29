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

package com.paydock.feature.address.presentation.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.KoinTestRule
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class CountryInputAutoCompleteTest : BaseViewModelKoinTest<CountryAutoCompleteViewModel>() {

    private val testModule: Module = module {
        viewModel { viewModel }

    }

    @get:Rule
    override val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule, testModule)
    )

    override fun initialiseViewModel(): CountryAutoCompleteViewModel = CountryAutoCompleteViewModel(dispatchersProvider)

    @Test
    fun testCountryAutoCompleteInitialStateInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CountryInputAutoComplete(
                    onCountrySelected = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("countrySearch").assertIsDisplayed()
    }

    @Test
    fun testCountryAutoCompleteReturnsExactResultAndUpdatesSelectedItem() {
        var selectedItem: String? = null
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CountryInputAutoComplete(
                    onCountrySelected = {
                        selectedItem = it
                    }
                )

            }
        }

        val mockCountry = "South Africa"

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("sdkInput")
            .performClick().apply {
                assertIsFocused()
                performTextInput("South Afr")
                assert(hasText("South Afr"))
            }

        composeTestRule.onNodeWithTag("searchResultsDropDown").assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(500L)

        composeTestRule.onNode(hasText(mockCountry), true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(mockCountry))
        // Verify that the selected item is updated
        assertNotNull(selectedItem)
        assertEquals(mockCountry, selectedItem)

    }

    @Test
    fun testCountryInputAutoCompleteReturnsSelectedAddressIsReturned() {
        val onCountrySelected: (String) -> Unit = mockk()

        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CountryInputAutoComplete(
                    onCountrySelected = onCountrySelected
                )

            }
        }

        val mockCountry = "South Africa"
        every { onCountrySelected(any()) } just Runs

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("sdkInput")
            .performClick().apply {
                assertIsFocused()
                performTextInput("South Afr")
                assert(hasText("South Afr"))
            }

        composeTestRule.onNodeWithTag("searchResultsDropDown").assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(500L)

        composeTestRule.onNode(hasText(mockCountry), true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(mockCountry))
        // Verify that the selected item is updated
        verify { onCountrySelected(mockCountry) }
    }
}