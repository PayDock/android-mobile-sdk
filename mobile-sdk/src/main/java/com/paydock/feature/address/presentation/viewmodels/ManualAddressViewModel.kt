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

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.ui.BaseViewModel
import com.paydock.feature.address.domain.model.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for handling the manual address input functionality.
 *
 * @param dispatchers Provides coroutines dispatchers for background tasks.
 */
internal class ManualAddressViewModel(dispatchers: DispatchersProvider) :
    BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<AddressDetailsViewState> =
        MutableStateFlow(AddressDetailsViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<AddressDetailsViewState> = _stateFlow.asStateFlow()

    fun setSavedAddress(address: BillingAddress? = null) {
        if (address != null) {
            updateState { state ->
                state.copy(
                    addressLine1 = address.addressLine1,
                    addressLine2 = address.addressLine2 ?: "",
                    city = address.city,
                    state = address.state,
                    postalCode = address.postalCode,
                    country = address.country,
                )
            }
        }
    }

    fun updateAddressLine1(addressLine: String) {
        updateState { state ->
            state.copy(
                addressLine1 = addressLine
            )
        }
    }

    fun updateAddressLine2(addressLine: String) {
        updateState { state ->
            state.copy(
                addressLine2 = addressLine
            )
        }
    }

    fun updateCity(city: String) {
        updateState { state ->
            state.copy(
                city = city
            )
        }
    }

    fun updateState(addressState: String) {
        updateState { state ->
            state.copy(
                state = addressState
            )
        }
    }

    fun updatePostalCode(postalCode: String) {
        updateState { state ->
            state.copy(
                postalCode = postalCode
            )
        }
    }

    fun updateCountry(country: String) {
        updateState { state ->
            state.copy(
                country = country
            )
        }
    }

    private fun updateState(update: (AddressDetailsViewState) -> AddressDetailsViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }

}