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
internal class CountryAutoCompleteViewModelTest : BaseKoinUnitTest() {

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

    @Test
    fun `exact match is first and case-insensitive`() = runTest {
        val mockCountry = MobileSDKTestConstants.Address.MOCK_COUNTRY
        val query = MobileSDKTestConstants.Address.MOCK_COUNTRY.lowercase()
        val results = mutableListOf<String>()

        viewModel.searchItems(query).collect { countries ->
            results.addAll(countries)
        }

        assertTrue(results.isNotEmpty())
        assertEquals(mockCountry, results.first())
    }

    @Test
    fun `startsWith matches are prioritised over contains`() = runTest {
        // Query that has known countries starting with it (e.g., "South" -> South Africa, South Sudan, etc.)
        val query = "South"
        val results = mutableListOf<String>()

        viewModel.searchItems(query).collect { countries ->
            results.addAll(countries)
        }

        assertTrue(results.isNotEmpty())
        // The first item should start with the query (priority for startsWith)
        assertTrue(results.first().startsWith(query, ignoreCase = true))
    }

    @Test
    fun `contains only queries still return items and respect limit`() = runTest {
        // "land" should match many countries by containment (Finland, Iceland, Thailand, etc.)
        val query = "land"
        val results = mutableListOf<String>()

        viewModel.searchItems(query).collect { countries ->
            results.addAll(countries)
        }

        assertTrue(results.isNotEmpty())
        assertTrue(results.size <= MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS)
        // All returned items should contain the query (case-insensitive)
        assertTrue(results.all { it.contains(query, ignoreCase = true) })
    }

    @Test
    fun `blank query returns first MAX results in sorted order`() = runTest {
        val results = mutableListOf<String>()

        viewModel.searchItems("").collect { countries ->
            results.addAll(countries)
        }

        assertTrue(results.isNotEmpty())
        assertEquals(MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS, results.size)
        // Should be in ascending order since source list is sorted before truncation
        val sorted = results.sorted()
        assertEquals(sorted, results)
    }

}