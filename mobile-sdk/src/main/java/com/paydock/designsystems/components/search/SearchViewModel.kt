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

package com.paydock.designsystems.components.search

import androidx.lifecycle.viewModelScope
import com.paydock.core.DEBOUNCE_DELAY
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.ui.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * An abstract ViewModel class for handling search functionality.
 *
 * @param dispatchers Provider for coroutines dispatchers.
 * @param T Type of search items.
 */
internal abstract class SearchViewModel<T>(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // MutableStateFlow for tracking search text
    private val _searchText: MutableStateFlow<String> = MutableStateFlow("")

    // Expose search text as StateFlow
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // MutableStateFlow for tracking search status
    private val _isSearching: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // Expose search status as StateFlow
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Flag to track whether initial loading is completed
    private var initialLoadCompleted = false

    /**
     * StateFlow that emits search results.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResult: StateFlow<List<T>> = _searchText
        .onEach { _isSearching.update { shouldSkipInitialLoading().not() } }
        .debounce(DEBOUNCE_DELAY)
        .flatMapLatest { query -> searchItems(query) }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
     * Checks whether the initial loading should be skipped.
     *
     * @return True if the initial loading should be skipped, false otherwise.
     */
    private fun shouldSkipInitialLoading(): Boolean {
        if (!initialLoadCompleted) {
            initialLoadCompleted = true
            return true
        }
        return false
    }

    /**
     * Sets the selected value and updates the search text.
     *
     * @param selectedValue The initial value to set.
     */
    fun setSelectedValue(selectedValue: String) {
        _searchText.value = selectedValue
    }

    /**
     * Called when the search text changes.
     *
     * @param text The new search text.
     */
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}