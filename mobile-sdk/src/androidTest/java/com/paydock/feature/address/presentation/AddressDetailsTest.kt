package com.paydock.feature.address.presentation

import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.address.domain.model.AddressDetailsEventNames
import com.paydock.feature.address.domain.model.integration.AddressDetailsWidgetConfig
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
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
import kotlin.test.assertTrue

@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class AddressDetailsTest : BaseViewModelKoinTest<AddressDetailsViewModel>() {

    private lateinit var geocoder: Geocoder

    private val testModule: Module = module {
        viewModel { AddressSearchViewModel(geocoder, dispatchersProvider) }
        viewModel { CountryAutoCompleteViewModel(dispatchersProvider) }
        viewModel { AddressDetailsViewModel(dispatchersProvider) }
        viewModel { viewModel }
    }

    override fun initialiseViewModel(): AddressDetailsViewModel =
        AddressDetailsViewModel(dispatchers = dispatchersProvider)

    @Before
    override fun onStart() {
        geocoder = mockk(relaxed = true)
        super.onStart()
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
            firstName = "John",
            lastName = "Doe",
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
                    config = AddressDetailsWidgetConfig(address = savedAddress),
                    completion = {}
                )
            }
        }

        // Verify Manual Address Input elements and interactions
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

        // Ensure required name fields are populated to satisfy validation before enabling save
        composeTestRule.onNodeWithTag("firstName1Input").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameInput").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("Doe")

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
        composeTestRule.onNode(
            hasTestTag("sdkInput") and hasAnyAncestor(hasTestTag("addressSearch"))
        )
            .performClick()
            .assertIsFocused()
            .performTextInput("1 Park Avenue")
        composeTestRule.onNode(
            hasTestTag("sdkInput") and hasAnyAncestor(hasTestTag("addressSearch"))
        ).assert(hasText("1 Park Avenue"))
        // Allow dropdown to render
        composeTestRule.waitUntilTimeout(150)
        composeTestRule.onNodeWithTag("searchResultsDropDown", useUnmergedTree = true).assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(1300L)

        composeTestRule.onAllNodesWithText(
            "1 Park Avenue, Manchester, Greater Manchester, M11 5MW, United Kingdom",
            useUnmergedTree = true
        )[0]
            .assertIsDisplayed()
            .performClick()

        // After selection the search field is cleared by design; verify manual section now visible
        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed()

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
        // Fill required name fields to satisfy validation
        composeTestRule.onNodeWithTag("firstName1Input").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameInput").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("Doe")

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
        // Fill required name fields first to satisfy validation
        composeTestRule.onNodeWithTag("firstName1Input").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameInput").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("Doe")
        composeTestRule.onNodeWithTag("showManualAddressButton")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // Verify Manual Address Input elements and interactions
        composeTestRule.onNodeWithTag("manualAddress").assertIsDisplayed()

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
        composeTestRule.onAllNodesWithText("United Kingdom", useUnmergedTree = true)[0]
            .performClick()

        composeTestRule.onNodeWithTag("countryInput").assertIsDisplayed().onChild()
            .assert(hasText("United Kingdom"))

        // Allow some time for the UI to update
        composeTestRule.waitUntilTimeout(300)

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testAddressDetailsSavingReturnsBillingAddress() {
        val savedAddress = BillingAddress(
            firstName = "John",
            lastName = "Doe",
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
                    config = AddressDetailsWidgetConfig(address = savedAddress),
                    completion = onAddressResult
                )
            }
        }

        every { onAddressResult(any()) } just Runs

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()
            .performClick()

        verify { onAddressResult(any()) }
    }

    @Test
    fun testSaveButtonClickFiresEventDelegate() {
        val savedAddress = BillingAddress(
            firstName = "John",
            lastName = "Doe",
            addressLine1 = "1 Park Avenue",
            city = "Manchester",
            state = "Greater Manchester",
            postalCode = "M11 5MW",
            country = "United Kingdom"
        )
        val eventSlot = slot<Event>()
        val eventDelegate: WidgetEventDelegate = mockk(relaxed = true) {
            every { widgetEvent(capture(eventSlot)) } just Runs
        }

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    config = AddressDetailsWidgetConfig(address = savedAddress),
                    eventDelegate = eventDelegate,
                    completion = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()
            .performClick()
        composeTestRule.waitForIdle()

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is Event.ButtonEvent)
        val buttonEvent = capturedEvent as Event.ButtonEvent
        assertTrue(buttonEvent.name == AddressDetailsEventNames.SAVE_BUTTON)
        assertTrue(buttonEvent.action == EventAction.CLICK)
    }

    @Test
    fun testManualEntryButtonClickFiresEventDelegate() {
        val eventSlot = slot<Event>()
        val eventDelegate: WidgetEventDelegate = mockk(relaxed = true) {
            every { widgetEvent(capture(eventSlot)) } just Runs
        }

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    eventDelegate = eventDelegate,
                    completion = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("showManualAddressButton").assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is Event.ButtonEvent)
        val buttonEvent = capturedEvent as Event.ButtonEvent
        assertTrue(buttonEvent.name == AddressDetailsEventNames.MANUAL_ENTRY_BUTTON)
        assertTrue(buttonEvent.action == EventAction.CLICK)
    }

    @Test
    fun testAddressSearchNoResultsDisplaysEmptyDropdown() {
        composeTestRule.setContent {
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

        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onGeocode(emptyList())
        }

        composeTestRule.onNode(
            hasTestTag("sdkInput") and hasAnyAncestor(hasTestTag("addressSearch"))
        )
            .performClick()
            .assertIsFocused()
            .performTextInput("NonexistentAddressXYZ123")

        composeTestRule.waitUntilTimeout(1500L)

        composeTestRule.onNodeWithTag("searchResultsDropDown", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testAddressSearchInputCanBeCleared() {
        composeTestRule.setContent {
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

        val searchInput = hasTestTag("sdkInput") and hasAnyAncestor(hasTestTag("addressSearch"))

        composeTestRule.onNode(searchInput)
            .performClick()
            .assertIsFocused()
            .performTextInput("1 Park Avenue")

        composeTestRule.onNode(searchInput).assert(hasText("1 Park Avenue"))

        composeTestRule.onNode(searchInput).performTextClearance()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(searchInput).assert(hasText(""))
    }

    @Test
    fun testAddressDataPersistsAfterRecomposition() {
        composeTestRule.setContent {
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

        // Fill in first name and last name
        composeTestRule.onNodeWithTag("firstName1Input").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("John")
        composeTestRule.onNodeWithTag("lastNameInput").onChild()
            .performClick()
            .assertIsFocused()
            .performTextInput("Doe")

        // Expand manual address section
        composeTestRule.onNodeWithTag("showManualAddressButton")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Fill in address details
        composeTestRule.onNodeWithTag("addressLine1Input").onChild()
            .performClick()
            .performTextInput("123 Test Street")
        composeTestRule.onNodeWithTag("cityInput").onChild()
            .performClick()
            .performTextInput("Sydney")
        composeTestRule.onNodeWithTag("stateInput").onChild()
            .performClick()
            .performTextInput("NSW")
        composeTestRule.onNodeWithTag("postalCodeInput").onChild()
            .performClick()
            .performTextInput("2000")

        // Select country from dropdown
        composeTestRule.onNodeWithTag("countryInput").onChild()
            .performClick()
            .performTextInput("Austral")
        composeTestRule.waitUntilTimeout(500)
        composeTestRule.onAllNodesWithText("Australia", useUnmergedTree = true)[0]
            .performClick()

        composeTestRule.waitForIdle()

        // Verify data persists - all fields should still have their values
        composeTestRule.onNodeWithTag("firstName1Input").onChild()
            .assert(hasText("John"))
        composeTestRule.onNodeWithTag("lastNameInput").onChild()
            .assert(hasText("Doe"))
        composeTestRule.onNodeWithTag("addressLine1Input").onChild()
            .assert(hasText("123 Test Street"))
        composeTestRule.onNodeWithTag("cityInput").onChild()
            .assert(hasText("Sydney"))
        composeTestRule.onNodeWithTag("stateInput").onChild()
            .assert(hasText("NSW"))
        composeTestRule.onNodeWithTag("postalCodeInput").onChild()
            .assert(hasText("2000"))
        composeTestRule.onNodeWithTag("countryInput").onChild()
            .assert(hasText("Australia"))

        // Verify save button is enabled (all required fields filled)
        composeTestRule.onNodeWithTag("saveAddress").assertIsDisplayed().assertIsEnabled()

        // Trigger recomposition by interacting with UI and verify data still persists
        composeTestRule.onNodeWithTag("addressLine1Input").onChild()
            .performClick()
        composeTestRule.waitForIdle()

        // Verify all data is still present after recomposition
        composeTestRule.onNodeWithTag("firstName1Input").onChild()
            .assert(hasText("John"))
        composeTestRule.onNodeWithTag("lastNameInput").onChild()
            .assert(hasText("Doe"))
        composeTestRule.onNodeWithTag("addressLine1Input").onChild()
            .assert(hasText("123 Test Street"))
        composeTestRule.onNodeWithTag("cityInput").onChild()
            .assert(hasText("Sydney"))
        composeTestRule.onNodeWithTag("stateInput").onChild()
            .assert(hasText("NSW"))
        composeTestRule.onNodeWithTag("postalCodeInput").onChild()
            .assert(hasText("2000"))
        composeTestRule.onNodeWithTag("countryInput").onChild()
            .assert(hasText("Australia"))
    }

}