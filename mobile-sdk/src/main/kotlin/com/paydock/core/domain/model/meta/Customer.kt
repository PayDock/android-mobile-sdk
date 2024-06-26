package com.paydock.core.domain.model.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a customer in the Mastercard Checkout flow.
 *
 * @property email The email address of the customer.
 * @property firstName The first name of the customer.
 * @property lastName The last name of the customer.
 * @property phone The phone number of the customer, encapsulated in a Phone object.
 */
@Serializable
data class Customer(
    val email: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val phone: Phone? = null
)
