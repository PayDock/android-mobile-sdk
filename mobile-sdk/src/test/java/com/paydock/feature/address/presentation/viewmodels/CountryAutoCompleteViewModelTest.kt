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

package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MAX_SEARCH_RESULTS
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
        assertEquals(results.size, MAX_SEARCH_RESULTS)
    }

    @Test
    fun `searchItems returns filtered result`() = runTest {
        val mockCountry = "South Africa"
        val query = "South"
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
        val mockCountry = "South Africa"
        val query = "South Africa"
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