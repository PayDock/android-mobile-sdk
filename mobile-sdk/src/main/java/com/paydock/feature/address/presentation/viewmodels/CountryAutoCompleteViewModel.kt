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

import com.paydock.core.MAX_SEARCH_RESULTS
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.designsystems.components.search.SearchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

/**
 * ViewModel for handling country autocomplete functionality.
 *
 * @param dispatchers DispatchersProvider to manage coroutine dispatchers.
 */
internal class CountryAutoCompleteViewModel(
    dispatchers: DispatchersProvider
) : SearchViewModel<String>(dispatchers) {
    // List of all countries fetched from Locale.
    private val allCountries: List<String> = Locale.getISOCountries().mapNotNull { countryCode ->
        val locale = Locale("", countryCode)
        locale.displayCountry
    }.sorted()

    override fun stringResults(): List<String> = searchResult.value

    /**
     * Filters countries based on the provided search query.
     *
     * @param query The search query to filter countries.
     * @return Flow emitting the filtered list of countries.
     */
    override fun searchItems(query: String): Flow<List<String>> = flow {
        if (query.isNotBlank()) {
            val result = allCountries.filter { country ->
                country.contains(query, ignoreCase = true)
            }
            // This is to improve performance on the large list
            emit(result.take(MAX_SEARCH_RESULTS))
        } else {
            // This is to improve performance on the large list
            emit(allCountries.take(MAX_SEARCH_RESULTS))
        }
    }
}