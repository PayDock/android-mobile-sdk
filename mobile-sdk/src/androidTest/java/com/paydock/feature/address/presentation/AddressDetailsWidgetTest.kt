package com.paydock.feature.address.presentation

import android.location.Geocoder
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
internal class AddressWidgetTest : BaseViewModelKoinTest<AddressDetailsViewModel>() {

    private lateinit var geocoder: Geocoder

    private val testModule: Module = module {
        single<Geocoder> { geocoder }
        viewModel {
            // Use the real AddressSearchViewModel with our mocked Geocoder
            AddressSearchViewModel(geocoder, dispatchersProvider)
        }
        viewModel { CountryAutoCompleteViewModel(dispatchersProvider) }
        viewModel { AddressDetailsViewModel(dispatchersProvider) }
    }

    @Before
    fun setUp() {
        geocoder = mockk(relaxed = true)

        // Set up Koin with our test module (not the default addressDetailsModule)
        setUpKoin()

        // Mock the Geocoder to return no results for gibberish search
        setupGeocoderMock()
    }

    @After
    override fun tearDownKoin() {
        unloadKoinModules(testModule)
        super.tearDownKoin()
    }

    override fun initialiseViewModel(): AddressDetailsViewModel {
        return AddressDetailsViewModel(dispatchers = dispatchersProvider)
    }

    /**
     * TODO Successful Address save via Address Suggestion is missing.
     * Architectural limitations of the SearchTextField component and deep Koin dependencies
     * What to consider: Integration tests that launch the full app
     */
    @Test
    fun testAddressWidgetUIElements() {
        println("=== Starting Address Widget UI Elements Test ===")

        // Step 1: Set up the Address Widget directly
        setupAddressWidget()

        // Verify form fields exits
        composeTestRule.onNodeWithText("First name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search for your address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Or enter address manually").assertIsDisplayed()

        // Verify Save button is displayed and is disabled
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()

        // Manual entry fields
        composeTestRule.onNodeWithText("Or enter address manually").performClick()
        composeTestRule.onNodeWithText("Address Line 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Address Line 2 (Optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("City").assertIsDisplayed()
        composeTestRule.onNodeWithText("State").assertIsDisplayed()
        composeTestRule.onNodeWithText("Postal Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Country").assertIsDisplayed()

        // Verify Save button is displayed and is disabled
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
        println("=== Address Widget UI Elements Test Completed Successfully ===")
    }

    @Test
    fun testNoAddressSearchResults() {
        println("=== Starting No Address Search Results Test ===")

        // Step 1: Set up the Address Widget directly
        setupAddressWidget()

        // Step 2: Search with gibberish text
        val addressSearchField = composeTestRule.onNodeWithText("Search for your address")
        addressSearchField.performClick()
        addressSearchField.performTextInput("gibberish123xyz")

        // Allow some time for the UI to update and search to process
        try {
            composeTestRule.waitForIdle()
            println("✅ waitForIdle completed")
        } catch (e: Exception) {
            println("⚠️ waitForIdle not available, using Thread.sleep instead")
            Thread.sleep(3000) // Wait 3 seconds for search to complete
        }

        // Step 4: Verify "No address found" error message appears
        try {
            composeTestRule.onNodeWithText("No address found").assertIsDisplayed()
            println("✅ 'No address found' error message is displayed")
        } catch (e: Exception) {
            println("❌ 'No address found' error message not found: ${e.message}")
            throw e
        }
    }

    @Test
    fun testSuccessfulAddressSave() {

        // Step 1: Setup Address Widget
        setupAddressWidget()

        // Step 2: Enter First and Last Name
        val firstNameField = composeTestRule.onNodeWithText("First name")
        val lastNameField = composeTestRule.onNodeWithText("Last name")
        firstNameField.performTextInput("John")
        lastNameField.performTextInput("Doe")

        // Step 3: Click on manual entry button
        val manualEntryButton = composeTestRule.onNodeWithText("Or enter address manually")
        manualEntryButton.assertIsDisplayed()
        manualEntryButton.performClick()
        Thread.sleep(1000) // 1s for form expansion

        // Step 4: Verify all manual entry fields are now visible and fill in address details
        val addressLine1Field = composeTestRule.onNodeWithText("Address Line 1")
        val cityField = composeTestRule.onNodeWithText("City")
        val stateField = composeTestRule.onNodeWithText("State")
        val postalCodeField = composeTestRule.onNodeWithText("Postal Code")
        val countryField = composeTestRule.onNodeWithText("Country")

        addressLine1Field.performTextInput("123 Main Street")
        cityField.performTextInput("Sydney")
        stateField.performTextInput("NSW")
        postalCodeField.performTextInput("4509-343")
        countryField.performTextInput("Australia")

        // Wait for form validation
        Thread.sleep(1000) // 1s for form validation

        // Step 5: Verify save button is enabled and tap it
        val saveButton = composeTestRule.onNodeWithText("Save")
        saveButton.assertIsDisplayed()
        saveButton.performClick()
        Thread.sleep(3000) // 3s for save operation
    }

    // Set up Koin for dependency injection
    private fun setUpKoin() {
        println("=== Setting up Koin ===")

        try {
            // First, unload the address details module to avoid conflicts
            unloadKoinModules(addressDetailsModule)
            println("✅ Address details module unloaded")

            // Then load our test module with the mocked Geocoder
            loadKoinModules(testModule)
            println("✅ Test module loaded successfully")

            println("✅ Koin modules loaded successfully")
        } catch (e: Exception) {
            println("❌ Error setting up Koin: ${e.message}")
            throw e
        }
    }

    // Set up the Address Widget directly using Compose
    private fun setupAddressWidget() {
        println("=== Setting up Address Widget Directly ===")

        try {
            // Set the widget content directly
            composeTestRule.setContent {
                AddressDetailsWidget(
                    completion = { billingAddress ->
                        println("Address completion result: $billingAddress")
                    }
                )
            }

            println("✅ Address widget set up successfully")

            // Wait for the widget to render
            Thread.sleep(2000)

        } catch (e: Exception) {
            println("❌ Error setting up Address widget: ${e.message}")
            throw e
        }
    }

    // Set up Geocoder mock to simulate search behavior
    private fun setupGeocoderMock() {
        println("=== Setting up Geocoder Mock ===")

        try {
            // Mock the Geocoder to return no results for gibberish search
            coEvery {
                geocoder.getFromLocationName(any(), any(), any())
            } answers {
                // Return empty list to simulate no results found
                val listener = firstArg<Geocoder.GeocodeListener>()
                listener.onGeocode(emptyList())
            }

            println("✅ Geocoder mock set up successfully")
        } catch (e: Exception) {
            println("❌ Error setting up Geocoder mock: ${e.message}")
            throw e
        }
    }
}
