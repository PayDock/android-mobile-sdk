package com.paydock.feature.src.presentation.model

import kotlinx.serialization.Serializable

/**
 * Represents checkout data.
 *
 * @property token The token associated with the checkout.
 * @property type The type of checkout data.
 * @property checkoutData The card data associated with the checkout.
 */
@Serializable
internal data class CheckoutData(
    val token: String,
    val type: String,
    val checkoutData: CardData?
)