package com.paydock.sample.feature.shop.domain.model

import androidx.annotation.DrawableRes
import com.paydock.sample.R

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: ProductImage,
    val category: ProductCategory
)

enum class ProductImage(@DrawableRes val drawableRes: Int) {
    // Clothing
    JACKET(R.drawable.jacket),
    SWEATER(R.drawable.sweater),
    HOODIE(R.drawable.hoodie),
    SHORTS(R.drawable.shorts),
    COTTON_TSHIRT(R.drawable.cotton_tshirt),
    WINTER_COAT(R.drawable.winter_coat),
    PANTS(R.drawable.pants),
    DRESS(R.drawable.dress),

    // Accessories
    LEATHER_BELT(R.drawable.leather_belt),
    SILK_SCARF(R.drawable.silk_scarf),
    SUNGLASSES(R.drawable.sunglasses),
    CAP(R.drawable.cap),
    WALLET(R.drawable.wallet),
    CROSSBODY_BAG(R.drawable.crossbody_bag),
    BACKPACK(R.drawable.backpack),
    WATCH(R.drawable.watch),

    // Electronics
    HEADPHONES(R.drawable.headphones),
    SMARTWATCH(R.drawable.smartwatch),
    BLUETOOTH_SPEAKER(R.drawable.bluetooth_speaker),
    SMARTPHONE(R.drawable.smartphone),
    LAPTOP(R.drawable.laptop),
    TABLET(R.drawable.tablet),
    COFFEE_MAKER(R.drawable.coffee_maker),

    // Home & Living
    PILLOWS(R.drawable.pillows),
    TABLE_LAMP(R.drawable.table_lamp),
    SCALE(R.drawable.scale)
}

enum class ProductCategory(val displayName: String) {
    CLOTHING("Clothing"),
    ACCESSORIES("Accessories"),
    FOOTWEAR("Footwear"),
    ELECTRONICS("Electronics"),
    HOME("Home & Living");

    companion object {
        fun getAllCategories(): List<ProductCategory> = entries
    }
} 