package com.paydock.feature.address.presentation

import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
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
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import com.paydock.feature.address.presentation.viewmodels.ManualAddressViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
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

@Suppress("LongMethod")
@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class AddressDetailsTest : BaseViewModelKoinTest<AddressDetailsViewModel>() {

    private lateinit var geocoder: Geocoder

    private val testModule: Module = module {
        viewModel { AddressSearchViewModel(geocoder, dispatchersProvider) }
        viewModel { CountryAutoCompleteViewModel(dispatchersProvider) }
        viewModel { ManualAddressViewModel(dispatchersProvider) }
        viewModel { viewModel }
    }

    @get:Rule
    override val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule, testModule)
    )

    override fun initialiseViewModel(): AddressDetailsViewModel =
        AddressDetailsViewModel(dispatchers = dispatchersProvider)

    @Before
    override fun onStart() {
        geocoder = mockk(relaxed = true)
        super.onStart()
    }

    @Test
    fun testAddressDetailsInitialStateInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    completion = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithText("Find an address").assertIsDisplayed()
        composeTestRule.onNodeWithTag("addressSearch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("showManualAddressButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun testAddressDetailsExpandShowsManualAddressInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    completion = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("showManualAddressButton")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed()

    }

    @Test
    fun testAddressDetailsWithSavedAddressExpandsManualInputAndIsPopulatedAndEnablesSaveButton() {
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
                AddressDetailsWidget(
                    address = savedAddress,
                    completion = {}
                )
            }
        }

        // Verify Manual Address Input elements and interactions
        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed().assert(hasScrollAction())
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

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()

    }

    @Test
    fun testAddressDetailsSuccessfulSearchExpandsAndPopulatesManualAddressInputAndEnablesSaveButton() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    completion = {}
                )
            }
        }

        val mockAddress = mockk<Address>()
        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onGeocode(listOf(mockAddress))
        }
        coEvery {
            mockAddress.getAddressLine(0)
        } returns "1 Park Avenue, Manchester, Greater Manchester, M11 5MW, United Kingdom"
        every { mockAddress.featureName } returns "1"
        every { mockAddress.thoroughfare } returns "Park Avenue"
        every { mockAddress.locality } returns "Manchester"
        every { mockAddress.adminArea } returns "Greater Manchester"
        every { mockAddress.postalCode } returns "M11 5MW"
        every { mockAddress.countryName } returns "United Kingdom"

        // Verify address search elements and interactions
        composeTestRule.onNodeWithTag("addressSearch")
            .apply {
                onChild()
                    .performClick()
                    .assertIsFocused()
                    .performTextInput("1 Park Avenue")
            }
        composeTestRule.onNodeWithTag("addressSearch").onChild().assert(hasText("1 Park Avenue"))
        composeTestRule.onNodeWithTag("searchResultsDropDown").assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(100L)

        composeTestRule.onNode(hasText("Searching…"), true).assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(500L)

        composeTestRule.onNode(
            hasText("1 Park Avenue, Manchester, Greater Manchester, M11 5MW, United Kingdom"),
            true
        )
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("addressSearch").onChild()
            .assert(hasText("1 Park Avenue, Manchester, Greater Manchester, M11 5MW, United Kingdom"))

        // Verify Manual Address Input elements and interactions
        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed()

        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed().assert(hasScrollAction())
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

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testAddressDetailsWithManualAddressInputEnablesSaveButton() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    completion = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("showManualAddressButton")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // Verify Manual Address Input elements and interactions
        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed()

        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed().assert(hasScrollAction())

        composeTestRule.onNodeWithTag("addressLine1Input").assertIsDisplayed().onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("1 Park Avenue")
        composeTestRule.onNodeWithTag("addressLine1Input").assertIsDisplayed().onChild()
            .assert(hasText("1 Park Avenue"))
            .performImeAction()

        composeTestRule.onNodeWithTag("addressLine2Input").assertIsDisplayed().onChild()
            .assert(hasText(""))
            .performImeAction()

        composeTestRule.onNodeWithTag("cityInput").assertIsDisplayed().onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("Manchester")
        composeTestRule.onNodeWithTag("cityInput").assertIsDisplayed().onChild()
            .assert(hasText("Manchester"))
            .performImeAction()

        composeTestRule.onNodeWithTag("stateInput").assertIsDisplayed().onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("Greater Manchester")
        composeTestRule.onNodeWithTag("stateInput").assertIsDisplayed().onChild()
            .assert(hasText("Greater Manchester"))
            .performImeAction()

        composeTestRule.onNodeWithTag("postalCodeInput").assertIsDisplayed().onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("M11 5MW")
        composeTestRule.onNodeWithTag("postalCodeInput").assertIsDisplayed().onChild()
            .assert(hasText("M11 5MW"))
            .performImeAction()

        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed().onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("United Kingd")

        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(500)

        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNodeWithText("United Kingdom").performClick()

        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed().onChild()
            .assert(hasText("United Kingdom"))

        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(300)

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testAddressDetailsSavingReturnsBillingAddress() {
        val savedAddress = BillingAddress(
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        val onAddressResult: (BillingAddress) -> Unit = mockk()
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    address = savedAddress,
                    completion = onAddressResult
                )
            }
        }

        every { onAddressResult(any()) } just Runs

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()
            .performClick()

        verify { onAddressResult(any()) }
    }

}