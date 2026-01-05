package com.paydock.sample.feature.checkout.domain.model

enum class CheckoutStep(val title: String) {
    INFORMATION("Information"),
    PAYMENT("Payment");

    companion object {
        fun getAllSteps(): List<CheckoutStep> = entries
    }
}

enum class PaymentMethod(val displayName: String, val iconRes: String) {
    CARD("Credit/Debit Card", "ic_card_filled"),
    GOOGLE_PAY("Google Pay", "ic_google_pay_default"),
    PAYPAL("PayPal", "ic_paypal"),
    AFTERPAY("Afterpay", "ic_afterpay"),
    CLICK_TO_PAY("Click to Pay", "ic_src"),
    COLES_PAY("Coles Pay", "ic_coles_pay"),
    ZIP("Zip", "ic_zip_widget");

    companion object {
        fun getAllMethods(): List<PaymentMethod> = entries
    }
}

data class ContactInfo(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = ""
) {
    val isComplete: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() &&
                email.isNotBlank() && phone.isNotBlank()

    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class Address(
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = ""
) {
    val isComplete: Boolean
        get() = addressLine1.isNotBlank() && city.isNotBlank() &&
                state.isNotBlank() && postalCode.isNotBlank() && country.isNotBlank()

    val formattedAddress: String
        get() = buildString {
            append(addressLine1)
            if (addressLine2?.isNotBlank() == true) {
                append("\n$addressLine2")
            }
            append("\n$city, $state $postalCode")
            if (country.isNotBlank()) {
                append("\n$country")
            }
        }
}

data class SavedAddress(
    val id: String,
    val label: String,
    val address: Address,
    val isDefault: Boolean = false,
    val firstName: String? = null,
    val lastName: String? = null,
) {
    val formattedAddress: String
        get() = address.formattedAddress
}

enum class AddressType {
    SHIPPING,
    BILLING
} 