package com.paydock.sample.feature.shop.domain.model

import java.text.NumberFormat
import java.util.Locale

enum class ShippingOption(
    val id: String,
    val shippingNmae: String,
    val description: String,
    val price: Double,
    val estimatedDays: String
) {
    STANDARD("standard", "Standard Shipping", "5-7 business days", 0.0, "5-7 days"),
    EXPRESS("express", "Express Shipping", "2-3 business days", 9.99, "2-3 days"),
    OVERNIGHT("overnight", "Overnight Shipping", "Next business day", 19.99, "1 day");

    val formattedPrice: String
        get() = if (price == 0.0) "Free" else NumberFormat.getCurrencyInstance(Locale.US)
            .format(price)

    companion object {
        fun getAllOptions(): List<ShippingOption> = values().toList()
    }
}

data class GiftCard(
    val id: String,
    val cardNumber: String,
    val balance: Double,
    val isActive: Boolean = true
) {
    val maskedCardNumber: String
        get() = "**** **** **** ${cardNumber.takeLast(4)}"

    val formattedBalance: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(balance)
}

data class AppliedGiftCard(
    val giftCard: GiftCard,
    val appliedAmount: Double
) {
    val formattedAppliedAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(appliedAmount)
} 