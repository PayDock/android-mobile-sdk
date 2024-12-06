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
 * ViewModel responsible for managing the address details input and processing.
 *
 * @param dispatchers The [DispatchersProvider] to manage coroutines' dispatchers.
 */
internal class AddressDetailsViewModel(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<AddressDetailsInputState> =
        MutableStateFlow(AddressDetailsInputState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<AddressDetailsInputState> = _stateFlow.asStateFlow()

    /**
     * Update the ViewModel's state with the provided default address.
     *
     * @param address The [BillingAddress] representing the default address.
     */
    fun updateDefaultAddress(address: BillingAddress) {
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

    /**
     * Update the ViewModel's state with the provided manual address details.
     *
     * @param addressState The [AddressDetailsInputState] representing the manual address details.
     */
    fun updateManualAddress(addressState: AddressDetailsInputState) {
        updateState { state ->
            state.copy(
                addressLine1 = addressState.addressLine1,
                addressLine2 = addressState.addressLine2,
                city = addressState.city,
                state = addressState.state,
                postalCode = addressState.postalCode,
                country = addressState.country,
            )
        }
    }

    /**
     * Update the ViewModel's state using the provided update function.
     *
     * @param update The update function to modify the current state.
     */
    private fun updateState(update: (AddressDetailsInputState) -> AddressDetailsInputState) {
        _stateFlow.update { state ->
            update(state)
        }
    }
}