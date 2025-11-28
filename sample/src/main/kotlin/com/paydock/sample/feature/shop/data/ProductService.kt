package com.paydock.sample.feature.shop.data

import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ProductCategory
import com.paydock.sample.feature.shop.domain.model.ProductImage

class ProductService private constructor() {

    fun getAllProducts(): List<Product> = sampleProducts

    private val sampleProducts = listOf(
        // Clothing
        Product(
            id = "1",
            name = "Classic Denim Jacket",
            description = "Premium denim jacket with classic fit and vintage styling",
            price = 89.99,
            image = ProductImage.JACKET,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "2",
            name = "Cotton Sweater",
            description = "Soft cotton blend sweater perfect for any season",
            price = 65.50,
            image = ProductImage.SWEATER,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "3",
            name = "Casual Hoodie",
            description = "Comfortable hoodie made from organic cotton",
            price = 49.99,
            image = ProductImage.HOODIE,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "4",
            name = "Summer Shorts",
            description = "Lightweight shorts perfect for warm weather",
            price = 35.00,
            image = ProductImage.SHORTS,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "5",
            name = "Cotton T-Shirt",
            description = "Classic cotton t-shirt in various colors",
            price = 24.99,
            image = ProductImage.COTTON_TSHIRT,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "6",
            name = "Winter Coat",
            description = "Warm winter coat with insulated lining",
            price = 179.99,
            image = ProductImage.WINTER_COAT,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "7",
            name = "Casual Pants",
            description = "Comfortable casual pants for everyday wear",
            price = 54.99,
            image = ProductImage.PANTS,
            category = ProductCategory.CLOTHING
        ),
        Product(
            id = "8",
            name = "Summer Dress",
            description = "Light and breezy summer dress",
            price = 69.99,
            image = ProductImage.DRESS,
            category = ProductCategory.CLOTHING
        ),

        // Accessories
        Product(
            id = "9",
            name = "Leather Belt",
            description = "Genuine leather belt with brushed metal buckle",
            price = 45.00,
            image = ProductImage.LEATHER_BELT,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "10",
            name = "Silk Scarf",
            description = "Luxurious silk scarf available in multiple colors",
            price = 39.99,
            image = ProductImage.SILK_SCARF,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "11",
            name = "Designer Sunglasses",
            description = "UV protection sunglasses with premium frame",
            price = 120.00,
            image = ProductImage.SUNGLASSES,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "12",
            name = "Baseball Cap",
            description = "Adjustable baseball cap with embroidered logo",
            price = 29.99,
            image = ProductImage.CAP,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "13",
            name = "Leather Wallet",
            description = "Premium leather wallet with multiple card slots",
            price = 55.00,
            image = ProductImage.WALLET,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "14",
            name = "Crossbody Bag",
            description = "Stylish crossbody bag perfect for daily use",
            price = 85.00,
            image = ProductImage.CROSSBODY_BAG,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "15",
            name = "Travel Backpack",
            description = "Durable backpack with laptop compartment",
            price = 95.00,
            image = ProductImage.BACKPACK,
            category = ProductCategory.ACCESSORIES
        ),
        Product(
            id = "16",
            name = "Classic Watch",
            description = "Elegant timepiece with leather strap",
            price = 249.99,
            image = ProductImage.WATCH,
            category = ProductCategory.ACCESSORIES
        ),

        // Electronics
        Product(
            id = "17",
            name = "Wireless Headphones",
            description = "High-quality wireless headphones with noise cancellation",
            price = 199.99,
            image = ProductImage.HEADPHONES,
            category = ProductCategory.ELECTRONICS
        ),
        Product(
            id = "18",
            name = "Smart Watch",
            description = "Feature-rich smartwatch with health monitoring",
            price = 299.00,
            image = ProductImage.SMARTWATCH,
            category = ProductCategory.ELECTRONICS
        ),
        Product(
            id = "19",
            name = "Bluetooth Speaker",
            description = "Compact Bluetooth speaker with premium sound quality",
            price = 79.99,
            image = ProductImage.BLUETOOTH_SPEAKER,
            category = ProductCategory.ELECTRONICS
        ),
        Product(
            id = "20",
            name = "Smartphone",
            description = "Latest smartphone with advanced camera system",
            price = 799.00,
            image = ProductImage.SMARTPHONE,
            category = ProductCategory.ELECTRONICS
        ),
        Product(
            id = "21",
            name = "Laptop Computer",
            description = "High-performance laptop for work and entertainment",
            price = 1299.00,
            image = ProductImage.LAPTOP,
            category = ProductCategory.ELECTRONICS
        ),
        Product(
            id = "22",
            name = "Tablet Device",
            description = "Versatile tablet with large display",
            price = 499.00,
            image = ProductImage.TABLET,
            category = ProductCategory.ELECTRONICS
        ),
        Product(
            id = "23",
            name = "Coffee Maker",
            description = "Programmable coffee maker with thermal carafe",
            price = 89.99,
            image = ProductImage.COFFEE_MAKER,
            category = ProductCategory.ELECTRONICS
        ),

        // Home & Living
        Product(
            id = "24",
            name = "Decorative Pillows",
            description = "Set of decorative throw pillows",
            price = 49.99,
            image = ProductImage.PILLOWS,
            category = ProductCategory.HOME
        ),
        Product(
            id = "25",
            name = "Table Lamp",
            description = "Modern table lamp with adjustable brightness",
            price = 64.99,
            image = ProductImage.TABLE_LAMP,
            category = ProductCategory.HOME
        ),
        Product(
            id = "26",
            name = "Digital Scale",
            description = "Precision digital scale for kitchen or bathroom",
            price = 34.99,
            image = ProductImage.SCALE,
            category = ProductCategory.HOME
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