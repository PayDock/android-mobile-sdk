package com.paydock.feature.address.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsFormState
import com.paydock.feature.address.presentation.state.AddressDetailsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel responsible for managing the address details input and processing.
 *
 * @param dispatchers The [DispatchersProvider] to manage coroutines' dispatchers.
 */
internal class AddressDetailsViewModel(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    private val _formState = MutableStateFlow(AddressDetailsFormState())

    // Flow to determine if the current form data is valid
    private val _isDataValid = combine(
        _formState
    ) { (currentFormState) ->
        // Basic validation: ensure required fields are not blank
        currentFormState.firstName.isNotBlank() &&
            currentFormState.lastName.isNotBlank() &&
            currentFormState.addressLine1.isNotBlank() &&
            currentFormState.city.isNotBlank() &&
            currentFormState.state.isNotBlank() &&
            currentFormState.postalCode.isNotBlank() &&
            currentFormState.country.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Combined UI State to be exposed to the Composable
    val stateFlow: StateFlow<AddressDetailsState> =
        combine(_formState, _isDataValid) { form, isValid ->
            AddressDetailsState(formState = form, isDataValid = isValid)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddressDetailsState()
        )

    /**
     * Triggers validation for all required fields in the form.
     * Used when the primary button is enabled by default (activePrimaryButton) so errors surface on click.
     */
    fun validateAllFields() {
        _formState.value = _formState.value.copy(
            firstNameErrorOverwrite = true,
            lastNameErrorOverwrite = true,
            addressLine1ErrorOverwrite = true,
            cityErrorOverwrite = true,
            stateErrorOverwrite = true,
            postcodeErrorOverwrite = true
        )
    }

    // --- Functions to update individual fields ---

    fun updateFirstName(firstName: String) {
        _formState.value = _formState.value.copy(firstName = firstName, firstNameErrorOverwrite = false)
    }

    fun updateLastName(lastName: String) {
        _formState.value = _formState.value.copy(lastName = lastName, lastNameErrorOverwrite = false)
    }

    fun updateAddressLine1(addressLine1: String) {
        _formState.value = _formState.value.copy(addressLine1 = addressLine1, addressLine1ErrorOverwrite = false)
    }

    fun updateAddressLine2(addressLine2: String) {
        _formState.value = _formState.value.copy(addressLine2 = addressLine2)
    }

    fun updateCity(city: String) {
        _formState.value = _formState.value.copy(city = city, cityErrorOverwrite = false)
    }

    fun updateState(stateValue: String) { // Renamed from 'state' to avoid conflict
        _formState.value = _formState.value.copy(state = stateValue, stateErrorOverwrite = false)
    }

    fun updatePostalCode(postalCode: String) {
        _formState.value = _formState.value.copy(postalCode = postalCode, postcodeErrorOverwrite = false)
    }

    fun updateCountry(country: String?) {
        _formState.value = _formState.value.copy(country = country ?: "")
    }

    /**
     * Populates the form fields from a BillingAddress object.
     * This is useful for pre-filling the form with an existing address or a search result.
     */
    fun populateFormWithBillingAddress(address: BillingAddress) {
        val currentForm = _formState.value

        _formState.value = currentForm.copy(
            firstName = address.firstName?.takeIf { it.isNotBlank() } ?: currentForm.firstName,
            lastName = address.lastName?.takeIf { it.isNotBlank() } ?: currentForm.lastName,

            addressLine1 = address.addressLine1 ?: currentForm.addressLine1,
            addressLine2 = address.addressLine2 ?: currentForm.addressLine2,
            city = address.city ?: currentForm.city,
            state = address.state ?: currentForm.state,
            postalCode = address.postalCode ?: currentForm.postalCode,
            country = address.country ?: currentForm.country
        )
    }

    /**
     * Clears all form fields.
     */
    fun clearForm() {
        _formState.value = AddressDetailsFormState()
    }

    /**
     * Returns the current form state as a BillingAddress object.
     * This should ideally only be called when isDataValid is true.
     */
    fun getBillingAddress(): BillingAddress {
        return _formState.value.toBillingAddress()
    }
}