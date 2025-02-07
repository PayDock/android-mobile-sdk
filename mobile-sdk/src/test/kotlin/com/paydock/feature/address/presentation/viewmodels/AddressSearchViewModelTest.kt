package com.paydock.feature.address.presentation.viewmodels

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
internal class AddressSearchViewModelTest : BaseKoinUnitTest() {

    private lateinit var viewModel: AddressSearchViewModel
    private lateinit var geocoder: Geocoder
    private val dispatchersProvider: DispatchersProvider by inject()

    @Before
    fun setup() {
        geocoder = mockk()
        viewModel = AddressSearchViewModel(geocoder, dispatchersProvider)
    }

    @Test
    fun `searchText is updated correctly`() {
        viewModel.onSearchTextChange("NewQuery")
        assertEquals("NewQuery", viewModel.searchText.value)
    }

    @Test
    fun `searchItems handles blank query`() = runTest {
        val query = ""
        val results = mutableListOf<List<Address>>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.add(addresses)
        }

        // Check that the results are empty for a blank query
        assert(results[0].isEmpty())
    }

    @Test
    fun `searchItems SDK_INT above or equal TIRAMISU returns expected results`() = runTest {
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.TIRAMISU
        )
        val mockAddress = mockk<Address>()
        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        // Mock the behavior of geocoder.getFromLocationName() and capture the GeocodeListener parameter
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onGeocode(listOf(mockAddress))
        }

        val query = "Test Address"
        val results = mutableListOf<Address>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.addAll(addresses)
        }

        // Check that the results contain the mock address
        assertTrue(results.isNotEmpty())
        assertTrue(results.contains(mockAddress))

        coVerify {
            geocoder.getFromLocationName(query, any(), any())
        }
    }

    @Test
    fun `searchItems SDK_INT above or equal TIRAMISU handles error`() = runTest {
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.TIRAMISU
        )
        val geocodeListenerSlot = slot<Geocoder.GeocodeListener>()
        val error = "Address not found"
        // Mock the behavior of geocoder.getFromLocationName() and capture the GeocodeListener parameter
        coEvery {
            geocoder.getFromLocationName(any(), any(), capture(geocodeListenerSlot))
        } answers {
            val listener = geocodeListenerSlot.captured
            listener.onError(error)
        }

        val query = "Test Address"
        val results = mutableListOf<Address>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.addAll(addresses)
        }

        // Check that the results contain the mock address
        assertTrue(results.isEmpty())

        coVerify {
            geocoder.getFromLocationName(query, any(), any())
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `searchItems SDK_INT below TIRAMISU returns expected results`() = runTest {
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.M
        )
        val mockAddress = mockk<Address>()
        coEvery {
            geocoder.getFromLocationName(any(), any())
        } returns listOf(mockAddress)

        val query = "Test Address"
        val results = mutableListOf<Address>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.addAll(addresses)
        }

        // Check that the results are empty due to the error
        assertTrue(results.isNotEmpty())
        assertTrue(results.contains(mockAddress))

        coVerify {
            geocoder.getFromLocationName(query, any())
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `searchItems SDK_INT below TIRAMISU returns null and defaults to empty results`() =
        runTest {
            setStaticFieldViaReflection(
                Build.VERSION::class.java.getDeclaredField("SDK_INT"),
                Build.VERSION_CODES.M
            )
            coEvery {
                geocoder.getFromLocationName(any(), any())
            } returns null

            val query = "Test Address"
            val results = mutableListOf<Address>()

            // Collect the results from the Flow
            viewModel.searchItems(query).collect { addresses ->
                results.addAll(addresses)
            }

            // Check that the results are empty due to the error
            assertTrue(results.isEmpty())

            coVerify {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, any())
            }
        }

    @Suppress("DEPRECATION")
    @Test
    fun `searchItems SDK_INT below TIRAMISU handles error`() = runTest {
        setStaticFieldViaReflection(
            Build.VERSION::class.java.getDeclaredField("SDK_INT"),
            Build.VERSION_CODES.M
        )
        coEvery {
            geocoder.getFromLocationName(any(), any())
        } throws Exception("Test Error")

        val query = "Test Address"
        val results = mutableListOf<Address>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.addAll(addresses)
        }

        // Check that the results are empty due to the error
        assertTrue(results.isEmpty())

        coVerify {
            geocoder.getFromLocationName(query, any())
        }
    }

}