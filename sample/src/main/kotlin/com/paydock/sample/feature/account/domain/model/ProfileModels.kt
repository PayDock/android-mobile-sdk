package com.paydock.sample.feature.account.domain.model

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val savedAddresses: List<SavedAddress> = emptyList()
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val isComplete: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() &&
                email.isNotBlank() && phone.isNotBlank()
}

data class SavedAddress(
    val id: String = generateId(),
    val label: String,
    val firstName: String = "",
    val lastName: String = "",
    val addressLine1: String,
    val addressLine2: String = "",
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean = false
) {
    val formattedAddress: String
        get() = buildString {
            append(addressLine1)
            if (addressLine2.isNotBlank()) {
                append("\n$addressLine2")
            }
            append("\n$city, $state $postalCode")
            if (country.isNotBlank()) {
                append("\n$country")
            }
        }

    val isComplete: Boolean
        get() = addressLine1.isNotBlank() && city.isNotBlank() &&
                state.isNotBlank() && postalCode.isNotBlank() && country.isNotBlank()

    companion object {
        fun generateId(): String = "addr_${System.currentTimeMillis()}"
    }
}

data class CheckoutData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: SavedAddress?
) 