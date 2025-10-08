package com.paydock.sample.feature.shop.domain.model

import java.text.NumberFormat
import java.util.Locale

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPrice: Double
        get() = product.price * quantity

    val formattedTotalPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(totalPrice)
}