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

import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import com.paydock.core.MAX_SEARCH_RESULTS
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.designsystems.components.search.SearchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ViewModel for handling the address search functionality.
 *
 * @param geocoder The Geocoder instance for performing address searches.
 * @param dispatchers Provides coroutines dispatchers for background tasks.
 */
internal class AddressSearchViewModel(
    private val geocoder: Geocoder,
    dispatchers: DispatchersProvider
) : SearchViewModel<Address>(dispatchers) {
    override fun stringResults(): List<String> = searchResult.value.mapNotNull { it.getAddressLine(0) }

    /**
     * Performs an address search and returns the search results as a Flow.
     *
     * @param query The search query.
     * @return A Flow emitting the search results as a Result.
     */
    @Suppress("DEPRECATION", "TooGenericExceptionCaught", "SwallowedException")
    override fun searchItems(query: String): Flow<List<Address>> = flow {
        if (query.isNotBlank()) {
            try {
                // Use suspendCoroutine to bridge between callback-based and coroutine-based APIs
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine<List<Address>> { continuation ->
                        // Call the geocoder with a custom GeocodeListener
                        geocoder.getFromLocationName(
                            query,
                            MAX_SEARCH_RESULTS,
                            object : GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    // Resume the continuation with the search results
                                    continuation.resume(addresses.toList())
                                }

                                override fun onError(errorMessage: String?) {
                                    // Resume the continuation with an empty list on error
                                    continuation.resume(emptyList())
                                }
                            }
                        )
                    }
                } else {
                    // Call the geocoder directly for older Android versions
                    geocoder.getFromLocationName(query, MAX_SEARCH_RESULTS)?.toList()
                        ?: emptyList()
                }
                // Emit a successful result with the addresses list
                emit(addresses)
            } catch (e: Exception) {
                // Emit empty result on exception
                emit(emptyList())
            }
        } else {
            // Emit a success result with an empty list for blank queries
            emit(emptyList())
        }
    }
}