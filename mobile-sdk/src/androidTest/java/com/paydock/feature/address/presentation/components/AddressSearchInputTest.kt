package com.paydock.feature.address.presentation.components

import android.location.Address
import android.location.Geocoder
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
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class AddressSearchInputTest : BaseViewModelKoinTest<AddressSearchViewModel>() {

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

    private lateinit var geocoder: Geocoder

    @Before
    override fun onStart() {
        geocoder = mockk(relaxed = true)
        super.onStart()
    }

    override fun initialiseViewModel(): AddressSearchViewModel =
        AddressSearchViewModel(geocoder, dispatchersProvider)

    @Test
    fun testAddressSearchInitialStateInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressSearchInput(
                    onAddressSelected = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("addressSearch").assertIsDisplayed()
    }

    @Test
    fun testAddressSearchInputReturnsNoSearchResult() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressSearchInput(
                    onAddressSelected = { }
                )

            }
        }

        val mockAddress = mockk<Address>()
        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        // Mock the behavior of geocoder.getFromLocationName() and capture the GeocodeListener parameter
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onGeocode(listOf(mockAddress))
        }
        coEvery { mockAddress.getAddressLine(0) } returns null

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("sdkInput")
            .performClick().apply {
                assertIsFocused()
                performTextInput("123 no results")
                assert(hasText("123 no results"))
            }

        composeTestRule.onNodeWithTag("searchResultsDropDown").assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(100L)

        composeTestRule.onNode(hasText("Searching…"), true).assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(300L)

        composeTestRule.onNode(hasText("No address found"), true).assertIsDisplayed()
    }

    @Test
    fun testAddressSearchInputReturnsValidSearchResultAndUpdatesSelectedItem() {
        var selectedItem: Address? = null

        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressSearchInput(
                    onAddressSelected = {
                        selectedItem = it
                    }
                )
            }
        }

        val mockAddress = mockk<Address>()
        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        // Mock the behavior of geocoder.getFromLocationName() and capture the GeocodeListener parameter
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onGeocode(listOf(mockAddress))
        }
        coEvery { mockAddress.getAddressLine(0) } returns "123 Test, City, Country, 007"

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("sdkInput")
            .performClick().apply {
                assertIsFocused()
                performTextInput("123 test")
                assert(hasText("123 test"))
            }

        composeTestRule.onNodeWithTag("searchResultsDropDown").assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(100L)

        composeTestRule.onNode(hasText("Searching…"), true).assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(500L)

        composeTestRule.onNode(hasText("123 Test, City, Country, 007"), true)
            .assertIsDisplayed()
            .performClick()

        // Verify that the selected item is updated
        assertNotNull(selectedItem)
        assertEquals(mockAddress, selectedItem)
    }

    @Test
    fun testAddressSearchInputReturnsSelectedAddressIsReturned() {
        val onAddressResult: (Address) -> Unit = mockk()

        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressSearchInput(
                    onAddressSelected = onAddressResult
                )
            }
        }

        val mockAddress = mockk<Address>()
        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        // Mock the behavior of geocoder.getFromLocationName() and capture the GeocodeListener parameter
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onGeocode(listOf(mockAddress))
        }
        coEvery { mockAddress.getAddressLine(0) } returns "123 Test, City, Country, 007"
        every { onAddressResult(any()) } just Runs

        // Verify UI elements and interactions
        composeTestRule.onNodeWithTag("sdkInput")
            .performClick().apply {
                assertIsFocused()
                performTextInput("123 test")
                assert(hasText("123 test"))
            }

        composeTestRule.onNodeWithTag("searchResultsDropDown").assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(100L)

        composeTestRule.onNode(hasText("Searching…"), true).assertIsDisplayed()

        // Trigger delay for UI to update
        composeTestRule.waitUntilTimeout(500L)

        composeTestRule.onNode(hasText("123 Test, City, Country, 007"), true)
            .assertIsDisplayed()
            .performClick()

        verify { onAddressResult(mockAddress) }
    }
}