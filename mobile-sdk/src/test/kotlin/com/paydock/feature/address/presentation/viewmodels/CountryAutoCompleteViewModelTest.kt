package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class CountryAutoCompleteViewModelTest : BaseKoinUnitTest() {

    private lateinit var viewModel: CountryAutoCompleteViewModel
    private val dispatchersProvider: DispatchersProvider by inject()

    @Before
    fun setup() {
        viewModel = CountryAutoCompleteViewModel(dispatchersProvider)
    }

    @Test
    fun `searchText is updated correctly`() {
        viewModel.onSearchTextChange("NewQuery")
        assertEquals("NewQuery", viewModel.searchText.value)
    }

    @Test
    fun `searchItems handles default query returns full list`() = runTest {
        val query = ""
        val results = mutableListOf<String>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { countries ->
            results.addAll(countries)
        }

        // Check that the results are empty for a blank query
        assertTrue(results.isNotEmpty())
        assertEquals(results.size, MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS)
    }

    @Test
    fun `searchItems returns filtered result`() = runTest {
        val mockCountry = MobileSDKTestConstants.Address.MOCK_COUNTRY
        val query = MobileSDKTestConstants.Address.MOCK_QUERY
        val results = mutableListOf<String>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.addAll(addresses)
        }

        // Check that the results contain the mock address
        assertTrue(results.isNotEmpty())
        assertTrue(results.size > 1)
        assertTrue(results.contains(mockCountry))
    }

    @Test
    fun `searchItems returns exact result`() = runTest {
        val mockCountry = MobileSDKTestConstants.Address.MOCK_COUNTRY
        val query = MobileSDKTestConstants.Address.MOCK_COUNTRY
        val results = mutableListOf<String>()

        // Collect the results from the Flow
        viewModel.searchItems(query).collect { addresses ->
            results.addAll(addresses)
        }

        // Check that the results contain the mock address
        assertTrue(results.isNotEmpty())
        assertTrue(results.size == 1)
        assertTrue(results.contains(mockCountry))
    }

}