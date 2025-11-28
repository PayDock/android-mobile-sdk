package com.paydock.sample.feature.account.data

import com.paydock.sample.feature.account.domain.model.CheckoutData
import com.paydock.sample.feature.account.domain.model.SavedAddress
import com.paydock.sample.feature.account.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProfileManager private constructor() {

    private val _profile = MutableStateFlow(
        UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+1 (555) 123-4567",
            savedAddresses = getSampleAddresses()
        )
    )
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ) {
        _profile.value = _profile.value.copy(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone
        )
    }

    fun addAddress(address: SavedAddress) {
        val currentAddresses = _profile.value.savedAddresses.toMutableList()

        // If this is set as default, remove default from other addresses
        if (address.isDefault) {
            for (i in currentAddresses.indices) {
                currentAddresses[i] = currentAddresses[i].copy(isDefault = false)
            }
        }

        // Add to the top of the list for better UX
        currentAddresses.add(0, address)
        _profile.value = _profile.value.copy(savedAddresses = currentAddresses)
    }

    fun updateAddress(updatedAddress: SavedAddress) {
        val currentAddresses = _profile.value.savedAddresses.toMutableList()
        val index = currentAddresses.indexOfFirst { it.id == updatedAddress.id }

        if (index != -1) {
            // If this is set as default, remove default from other addresses
            if (updatedAddress.isDefault) {
                for (i in currentAddresses.indices) {
                    if (i != index) {
                        currentAddresses[i] = currentAddresses[i].copy(isDefault = false)
                    }
                }
            }

            currentAddresses[index] = updatedAddress
            _profile.value = _profile.value.copy(savedAddresses = currentAddresses)
        }
    }

    fun deleteAddress(address: SavedAddress) {
        val currentAddresses = _profile.value.savedAddresses.toMutableList()
        currentAddresses.removeAll { it.id == address.id }

        // If we deleted the default address and there are other addresses, make the first one default
        if (address.isDefault && currentAddresses.isNotEmpty()) {
            currentAddresses[0] = currentAddresses[0].copy(isDefault = true)
        }

        _profile.value = _profile.value.copy(savedAddresses = currentAddresses)
    }

    fun setAddressAsDefault(address: SavedAddress) {
        val currentAddresses = _profile.value.savedAddresses.toMutableList()

        // Remove default from all addresses
        for (i in currentAddresses.indices) {
            currentAddresses[i] = currentAddresses[i].copy(isDefault = false)
        }

        // Set the selected address as default
        val index = currentAddresses.indexOfFirst { it.id == address.id }
        if (index != -1) {
            currentAddresses[index] = currentAddresses[index].copy(isDefault = true)
            _profile.value = _profile.value.copy(savedAddresses = currentAddresses)
        }
    }

    fun populateCheckoutFromProfile(): CheckoutData {
        val profile = _profile.value
        val defaultAddress = profile.savedAddresses.find { it.isDefault }

        return CheckoutData(
            firstName = profile.firstName,
            lastName = profile.lastName,
            email = profile.email,
            phone = profile.phone,
            address = defaultAddress
        )
    }

    fun saveCheckoutDataToProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ) {
        updateProfile(firstName, lastName, email, phone)
    }

    private fun getSampleAddresses(): List<SavedAddress> = listOf(
        SavedAddress(
            id = "addr_1",
            label = "Home",
            firstName = "John",
            lastName = "Doe",
            addressLine1 = "123 Main Street",
            addressLine2 = "Apt 2B",
            city = "San Francisco",
            state = "CA",
            postalCode = "94102",
            country = "United States",
            isDefault = true
        ),
        SavedAddress(
            id = "addr_2",
            label = "Work",
            firstName = "John",
            lastName = "Doe",
            addressLine1 = "456 Market Street",
            addressLine2 = "Suite 1000",
            city = "San Francisco",
            state = "CA",
            postalCode = "94105",
            country = "United States",
            isDefault = false
        )
    )

    companion object {
        @Volatile
        private var INSTANCE: UserProfileManager? = null

        val shared: UserProfileManager
            get() {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: UserProfileManager().also { INSTANCE = it }
                }
            }
    }
} 