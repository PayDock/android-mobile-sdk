package com.paydock.sample.feature.shop.data

import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ProductCategory

class ProductService private constructor() {
    
    fun getAllProducts(): List<Product> = sampleProducts
    
    private val sampleProducts = listOf(
        // Clothing
        Product(
            id = "1",
            name = "Classic Denim Jacket",
            description = "Premium denim jacket with classic fit and vintage styling",
            price = 89.99,
            imageName = "demo_product_1",
            category = ProductCategory.CLOTHING,
            imageUrl = "https://picsum.photos/seed/denim/600/400"
        ),
        Product(
            id = "2",
            name = "Cotton Sweater",
            description = "Soft cotton blend sweater perfect for any season",
            price = 65.50,
            imageName = "demo_product_2",
            category = ProductCategory.CLOTHING,
            imageUrl = "https://picsum.photos/seed/sweater/600/400"
        ),
        Product(
            id = "3",
            name = "Casual Hoodie",
            description = "Comfortable hoodie made from organic cotton",
            price = 49.99,
            imageName = "ic_coles_pay",
            category = ProductCategory.CLOTHING,
            imageUrl = "https://picsum.photos/seed/hoodie/600/400"
        ),
        Product(
            id = "4",
            name = "Summer Shorts",
            description = "Lightweight shorts perfect for warm weather",
            price = 35.00,
            imageName = "ic_afterpay",
            category = ProductCategory.CLOTHING,
            imageUrl = "https://picsum.photos/seed/shorts/600/400"
        ),
        
        // Accessories
        Product(
            id = "5",
            name = "Leather Belt",
            description = "Genuine leather belt with brushed metal buckle",
            price = 45.00,
            imageName = "ic_paypal",
            category = ProductCategory.ACCESSORIES,
            imageUrl = "https://picsum.photos/seed/belt/600/400"
        ),
        Product(
            id = "6",
            name = "Wool Scarf",
            description = "Warm wool scarf available in multiple colors",
            price = 29.99,
            imageName = "ic_google_pay_default",
            category = ProductCategory.ACCESSORIES,
            imageUrl = "https://picsum.photos/seed/scarf/600/400"
        ),
        Product(
            id = "7",
            name = "Designer Sunglasses",
            description = "UV protection sunglasses with premium frame",
            price = 120.00,
            imageName = "ic_src",
            category = ProductCategory.ACCESSORIES,
            imageUrl = "https://picsum.photos/seed/sunglasses/600/400"
        ),
        
        // Footwear
        Product(
            id = "8",
            name = "Running Shoes",
            description = "Comfortable running shoes with excellent support",
            price = 95.00,
            imageName = "ic_launcher_foreground",
            category = ProductCategory.FOOTWEAR,
            imageUrl = "https://picsum.photos/seed/shoes/600/400"
        ),
        Product(
            id = "9",
            name = "Leather Boots",
            description = "Durable leather boots perfect for any occasion",
            price = 149.99,
            imageName = "demo_icon",
            category = ProductCategory.FOOTWEAR,
            imageUrl = "https://picsum.photos/seed/boots/600/400"
        ),
        
        // Electronics
        Product(
            id = "10",
            name = "Wireless Headphones",
            description = "High-quality wireless headphones with noise cancellation",
            price = 199.99,
            imageName = "ic_widgets",
            category = ProductCategory.ELECTRONICS,
            imageUrl = "https://picsum.photos/seed/headphones/600/400"
        ),
        Product(
            id = "11",
            name = "Smart Watch",
            description = "Feature-rich smartwatch with health monitoring",
            price = 299.00,
            imageName = "ic_style",
            category = ProductCategory.ELECTRONICS,
            imageUrl = "https://picsum.photos/seed/watch/600/400"
        ),
        Product(
            id = "12",
            name = "Portable Speaker",
            description = "Compact Bluetooth speaker with premium sound quality",
            price = 79.99,
            imageName = "ic_checkout",
            category = ProductCategory.ELECTRONICS,
            imageUrl = "https://picsum.photos/seed/speaker/600/400"
        )
    )
    
    companion object {
        @Volatile
        private var INSTANCE: ProductService? = null

        val shared: ProductService
            get() {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: ProductService().also { INSTANCE = it }
                }
            }
    }
} 