package com.paydock.sample.feature.checkout.data.api.dto

data class PaymentMethodOptions(
    val card: CardPaymentMethod = CardPaymentMethod()
)