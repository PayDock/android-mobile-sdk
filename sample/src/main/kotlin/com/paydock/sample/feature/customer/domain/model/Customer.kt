package com.paydock.sample.feature.customer.domain.model

data class Customer(
    val email: String?,
    val firstName: String,
    val lastName: String,
    val phone: String,
    // For now we do not need any other customer info
)
