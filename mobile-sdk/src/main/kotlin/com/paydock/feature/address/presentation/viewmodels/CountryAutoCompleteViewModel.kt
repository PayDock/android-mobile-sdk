package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.MobileSDKConstants
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
            val normalizedQuery = query.trim()
            // Prioritize exact matches, then startsWith, then contains, to ensure exact text
            // entries are included even when the list is truncated by MAX_SEARCH_RESULTS.
            val (exactMatches, remaining) = allCountries.partition {
                it.equals(normalizedQuery, ignoreCase = true)
            }
            val startsWithMatches = remaining.filter { it.startsWith(normalizedQuery, ignoreCase = true) }
            val containsMatches = remaining.filter {
                it.contains(normalizedQuery, ignoreCase = true) && !it.startsWith(normalizedQuery, ignoreCase = true)
            }
            val ordered = exactMatches + startsWithMatches + containsMatches
            emit(ordered.take(MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS))
        } else {
            emit(allCountries.take(MobileSDKConstants.AddressConfig.MAX_SEARCH_RESULTS))
        }
    }
}