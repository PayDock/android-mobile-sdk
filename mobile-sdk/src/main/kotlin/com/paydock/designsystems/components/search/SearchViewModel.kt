package com.paydock.designsystems.components.search

import androidx.lifecycle.viewModelScope
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * An abstract ViewModel class for handling search functionality.
 *
 * @param dispatchers Provider for coroutines dispatchers.
 * @param T Type of search items.
 */
internal abstract class SearchViewModel<T>(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // This holds the raw results from searchItems (could be List<String> or List<YourObjectType>)
    private val _searchResult = MutableStateFlow<List<T>>(emptyList())
    val searchResult: StateFlow<List<T>> = _searchResult.asStateFlow()

    /**
     * Get a list of string-based search results.
     *
     * @return List of search results as strings.
     */
    abstract fun stringResults(): List<String>

    /**
     * Perform a search for items based on the provided query.
     *
     * @param query The search query.
     * @return Flow emitting the list of search results.
     */
    abstract fun searchItems(query: String): Flow<List<T>>

    /**
     * Called when the search text changes.
     *
     * @param text The new search text.
     */
    @OptIn(FlowPreview::class)
    fun onSearchTextChange(text: String) {
        _searchText.value = text
        if (text.isNotBlank()) {
            viewModelScope.launch {
                _isSearching.value = true
                searchItems(text)
                    .debounce(MobileSDKConstants.General.INPUT_DELAY)
                    .distinctUntilChanged()
                    .collect { results ->
                        _searchResult.value = results
                        _isSearching.value = false
                    }
            }
        } else {
            _searchResult.value = emptyList() // Clear results if search text is blank
            // Or emit allCountries.take(...) if you want to show initial items on blank focused field
        }
    }
}