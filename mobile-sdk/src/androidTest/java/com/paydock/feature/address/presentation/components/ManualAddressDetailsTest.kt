package com.paydock.feature.address.presentation.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.KoinTestRule
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.feature.address.domain.model.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsViewState
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import com.paydock.feature.address.presentation.viewmodels.ManualAddressViewModel
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
import kotlin.test.assertTrue

@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class ManualAddressDetailsTest : BaseViewModelKoinTest<ManualAddressViewModel>() {

    private val testModule: Module = module {
        viewModel { viewModel }
        viewModel { CountryAutoCompleteViewModel(dispatchersProvider) }
    }

    @get:Rule
    override val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule, testModule)
    )

    override fun initialiseViewModel(): ManualAddressViewModel =
        ManualAddressViewModel(dispatchers = dispatchersProvider)

    @Test
    fun testManualAddressDetailsInitialStateInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                ManualAddress(
                    onAddressUpdated = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("addressLine1Input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("addressLine2Input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cityInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stateInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("postalCodeInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed()
    }

    @Test
    fun testManualAddressDetailsStateWithSavedAddressInput() {
        val savedAddress = BillingAddress(
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                ManualAddress(
                    savedAddress = savedAddress,
                    onAddressUpdated = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("addressLine1Input").assertIsDisplayed().onChild().assert(
            hasText("1 Park Avenue")
        )
        composeTestRule.onNodeWithTag("addressLine2Input").assertIsDisplayed().onChild().assert(
            hasText("")
        )
        composeTestRule.onNodeWithTag("cityInput").assertIsDisplayed().onChild().assert(
            hasText("Manchester")
        )
        composeTestRule.onNodeWithTag("stateInput").assertIsDisplayed().onChild().assert(
            hasText("Greater Manchester")
        )
        composeTestRule.onNodeWithTag("postalCodeInput").assertIsDisplayed().onChild().assert(
            hasText("M11 5MW")
        )
        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed().onChild().assert(
            hasText("United Kingdom")
        )
    }

    @Suppress("LongMethod")
    @Test
    fun testManualAddressValidInputCallsOnAddressUpdated() {
        val onAddressUpdated: (AddressDetailsViewState) -> Unit = mockk()
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                ManualAddress(
                    onAddressUpdated = onAddressUpdated
                )
            }
        }
        every { onAddressUpdated(any()) } just Runs

        // Simulate user interactions
        composeTestRule.onNodeWithTag("addressLine1Input").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("1 Park Avenue")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("addressLine1Input").onChild().performImeAction()

        composeTestRule.onNodeWithTag("addressLine2Input").onChild()
            .assertIsFocused()
            .performImeAction()

        composeTestRule.onNodeWithTag("cityInput").onChild()
            .assertIsFocused()
            .performTextInput("Manchester")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("cityInput").onChild().performImeAction()

        composeTestRule.onNodeWithTag("stateInput").onChild()
            .assertIsFocused()
            .performTextInput("Greater Manchester")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("stateInput").onChild().performImeAction()

        composeTestRule.onNodeWithTag("postalCodeInput").onChild()
            .assertIsFocused()
            .performTextInput("M11 5MW")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("postalCodeInput").onChild().performImeAction()

        composeTestRule.onNodeWithTag("countryInput").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("United Kingd")

        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(500)

        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithText("United Kingdom").performClick()

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("addressLine1Input").assertIsDisplayed().onChild().assert(
            hasText("1 Park Avenue")
        )
        composeTestRule.onNodeWithTag("addressLine2Input").assertIsDisplayed().onChild().assert(
            hasText("")
        )
        composeTestRule.onNodeWithTag("cityInput").assertIsDisplayed().onChild().assert(
            hasText("Manchester")
        )
        composeTestRule.onNodeWithTag("stateInput").assertIsDisplayed().onChild().assert(
            hasText("Greater Manchester")
        )
        composeTestRule.onNodeWithTag("postalCodeInput").assertIsDisplayed().onChild().assert(
            hasText("M11 5MW")
        )
        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed().onChild().assert(
            hasText("United Kingdom")
        )
        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(300)
        // Verify actual updated address is passed back
        verify { onAddressUpdated(viewModel.stateFlow.value) }
    }

    @Suppress("LongMethod")
    @Test
    fun testManualAddressValidInputUpdatesAddressViewState() {
        var manualAddressState: AddressDetailsViewState? = null
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                ManualAddress(
                    onAddressUpdated = {
                        manualAddressState = it
                    }
                )
            }
        }
        // Simulate user interactions
        // ... Use composeTestRule.onNode and composeTestRule.onNodeWithContentDescription
        //     to interact with specific UI elements
        // Allow some time for the UI to update
        composeTestRule.onNodeWithTag("addressLine1Input").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("1 Park Avenue")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("addressLine1Input").onChild().performImeAction()

        composeTestRule.onNodeWithTag("addressLine2Input").onChild()
            .assertIsFocused()
            .performImeAction()
        // Send the IME action (e.g., Done) to the TextField

        composeTestRule.onNodeWithTag("cityInput").onChild()
            .assertIsFocused()
            .performTextInput("Manchester")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("cityInput").onChild().performImeAction()

        composeTestRule.onNodeWithTag("stateInput").onChild()
            .assertIsFocused()
            .performTextInput("Greater Manchester")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("stateInput").onChild().performImeAction()

        composeTestRule.onNodeWithTag("postalCodeInput").onChild()
            .assertIsFocused()
            .performTextInput("M11 5MW")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithTag("postalCodeInput").onChild().performImeAction()

        composeTestRule.onNodeWithTag("countryInput").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("United Kingd")

        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(500)

        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithText("United Kingdom").performClick()

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("addressLine1Input").assertIsDisplayed().onChild().assert(
            hasText("1 Park Avenue")
        )
        composeTestRule.onNodeWithTag("addressLine2Input").assertIsDisplayed().onChild().assert(
            hasText("")
        )
        composeTestRule.onNodeWithTag("cityInput").assertIsDisplayed().onChild().assert(
            hasText("Manchester")
        )
        composeTestRule.onNodeWithTag("stateInput").assertIsDisplayed().onChild().assert(
            hasText("Greater Manchester")
        )
        composeTestRule.onNodeWithTag("postalCodeInput").assertIsDisplayed().onChild().assert(
            hasText("M11 5MW")
        )
        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed().onChild().assert(
            hasText("United Kingdom")
        )

        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(300)

        // Assert ViewModel interactions
        assertTrue(viewModel.stateFlow.value.isDataValid)
        // Assert Manual Address Input state matches expected address
        assertEquals("1 Park Avenue", manualAddressState?.addressLine1)
        assertEquals("", manualAddressState?.addressLine2)
        assertEquals("Manchester", manualAddressState?.city)
        assertEquals("Greater Manchester", manualAddressState?.state)
        assertEquals("M11 5MW", manualAddressState?.postalCode)
        assertEquals("United Kingdom", manualAddressState?.country)
    }
}