package com.paydock.feature.address.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsInputState
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
    private val _stateFlow: MutableStateFlow<AddressDetailsInputState> =
        MutableStateFlow(AddressDetailsInputState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<AddressDetailsInputState> = _stateFlow.asStateFlow()

    fun setSavedAddress(address: BillingAddress? = null) {
        if (address != null) {
            updateState { state ->
                state.copy(
                    addressLine1 = address.addressLine1 ?: "",
                    addressLine2 = address.addressLine2 ?: "",
                    city = address.city ?: "",
                    state = address.state ?: "",
                    postalCode = address.postalCode ?: "",
                    country = address.country ?: "",
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

    private fun updateState(update: (AddressDetailsInputState) -> AddressDetailsInputState) {
        _stateFlow.update { state ->
            update(state)
        }
    }

}