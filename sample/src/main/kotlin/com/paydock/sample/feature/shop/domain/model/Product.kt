package com.paydock.sample.feature.shop.domain.model

import java.text.NumberFormat
import java.util.Locale

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageName: String?,
    val category: ProductCategory,
    val imageUrl: String? = null
) {
    val formattedPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale.US).format(price)
}

enum class ProductCategory(val displayName: String) {
    CLOTHING("Clothing"),
    ACCESSORIES("Accessories"),
    FOOTWEAR("Footwear"),
    ELECTRONICS("Electronics");

    companion object {
        fun getAllCategories(): List<ProductCategory> = entries
    }
} 