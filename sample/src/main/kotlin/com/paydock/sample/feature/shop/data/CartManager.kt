package com.paydock.sample.feature.shop.data

import com.paydock.sample.feature.shop.domain.model.AppliedGiftCard
import com.paydock.sample.feature.shop.domain.model.CartItem
import com.paydock.sample.feature.shop.domain.model.GiftCard
import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ShippingOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

class CartManager private constructor() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedGiftCards = MutableStateFlow<List<AppliedGiftCard>>(emptyList())
    val appliedGiftCards: StateFlow<List<AppliedGiftCard>> = _appliedGiftCards.asStateFlow()

    private val _selectedShipping = MutableStateFlow(ShippingOption.STANDARD)
    val selectedShipping: StateFlow<ShippingOption> = _selectedShipping.asStateFlow()

    val isEmpty: Boolean
        get() = _cartItems.value.isEmpty()

    val itemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.totalPrice }

    val hasGiftCards: Boolean
        get() = _appliedGiftCards.value.isNotEmpty()

    val totalGiftCardAmount: Double
        get() = _appliedGiftCards.value.sumOf { it.appliedAmount }

    val shippingCost: Double
        get() = _selectedShipping.value.price

    val totalPrice: Double
        get() = (subtotal + shippingCost - totalGiftCardAmount).coerceAtLeast(0.0)

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.product.id == product.id }

        if (existingItemIndex >= 0) {
            currentItems[existingItemIndex] = currentItems[existingItemIndex].copy(
                quantity = currentItems[existingItemIndex].quantity + quantity
            )
        } else {
            currentItems.add(CartItem(product, quantity))
        }

        _cartItems.value = currentItems
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.remove(cartItem)
        _cartItems.value = currentItems
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product.id == productId }

        if (itemIndex >= 0) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(quantity = quantity)
            _cartItems.value = currentItems
        }
    }

    fun updateQuantity(cartItem: CartItem, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(cartItem)
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOf(cartItem)

        if (itemIndex >= 0) {
            currentItems[itemIndex] = cartItem.copy(quantity = quantity)
            _cartItems.value = currentItems
        }
    }

    fun setSelectedShipping(option: ShippingOption) {
        _selectedShipping.value = option
    }

    fun applyGiftCard(giftCard: GiftCard): Boolean {
        // Check if already applied
        if (_appliedGiftCards.value.any { it.giftCard.id == giftCard.id }) {
            return false
        }

        // Calculate how much of the gift card to apply
        val remainingTotal =
            totalPrice + totalGiftCardAmount // Add back current gift cards to get original total
        val applicableAmount = min(giftCard.balance, remainingTotal)

        if (applicableAmount > 0) {
            val appliedGiftCard = AppliedGiftCard(giftCard, applicableAmount)
            _appliedGiftCards.value = _appliedGiftCards.value + appliedGiftCard
            return true
        }

        return false
    }

    fun removeGiftCard(appliedGiftCard: AppliedGiftCard) {
        _appliedGiftCards.value = _appliedGiftCards.value - appliedGiftCard
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedGiftCards.value = emptyList()
        _selectedShipping.value = ShippingOption.STANDARD
    }

    companion object {
        @Volatile
        private var INSTANCE: CartManager? = null

        val shared: CartManager
            get() {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: CartManager().also { INSTANCE = it }
                }
            }
    }
} 