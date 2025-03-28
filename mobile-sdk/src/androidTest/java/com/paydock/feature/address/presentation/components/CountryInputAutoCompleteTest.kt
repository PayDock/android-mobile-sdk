package com.paydock.feature.address.presentation.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(KoinInternalApi::class, ExperimentalComposeUiApi::class)
@RunWith(AndroidJUnit4::class)
internal class CountryInputAutoCompleteTest :
    BaseViewModelKoinTest<CountryAutoCompleteViewModel>() {

    private val testModule: Module = module {
        viewModel { viewModel }

    }

    @Before
    fun setUpKoin() {
        unloadKoinModules(addressDetailsModule)
        loadKoinModules(testModule)
    }

    @After
    override fun tearDownKoin() {
        unloadKoinModules(testModule)
        super.tearDownKoin()
    }

    override fun initialiseViewModel(): CountryAutoCompleteViewModel =
        CountryAutoCompleteViewModel(dispatchersProvider)

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