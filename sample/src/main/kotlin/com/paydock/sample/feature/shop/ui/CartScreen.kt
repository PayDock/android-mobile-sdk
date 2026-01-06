package com.paydock.sample.feature.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.sample.core.utils.CurrencyFormatter
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonShape
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.domain.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
    configViewModel: ConfigViewModel = hiltViewModel()
) {
    val globalConfig by configViewModel.globalConfig.collectAsState()
    val currencyCode = globalConfig.currencyCode
    val cartManager = remember { CartManager.shared }
    val cartItems by cartManager.cartItems.collectAsState()

    if (cartManager.isEmpty) {
        EmptyCartView(onContinueShopping = onContinueShopping)
    } else {
        CartContentView(
            cartManager = cartManager,
            cartItems = cartItems,
            currencyCode = currencyCode,
            onCheckout = onCheckout
        )
    }

}

@Composable
private fun EmptyCartView(
    onContinueShopping: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("empty_cart_view"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "Empty Cart",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag("empty_cart_title")
            )

            Text(
                text = "Add some products to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("empty_cart_message")
            )

            AppButton(
                text = "Continue Shopping",
                onClick = onContinueShopping,
                shape = AppButtonShape.Pill,
                modifier = Modifier
                    .width(200.dp)
                    .testTag("empty_cart_continue_shopping_button")
            )
        }
    }
}

@Composable
private fun CartContentView(
    cartManager: CartManager,
    cartItems: List<CartItem>,
    currencyCode: String,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Use LazyColumn for better performance with many items
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("cart_items_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cart Items Section Header
            item {
                CartItemsSectionHeader(cartManager = cartManager)
            }

            // Cart Items - lazy loaded for better performance
            items(
                items = cartItems,
                key = { it.product.id } // Use product ID as key for stable recomposition
            ) { cartItem ->
                CartItemRowView(
                    cartItem = cartItem,
                    currencyCode = currencyCode,
                    onQuantityChange = { updatedItem, newQuantity ->
                        cartManager.updateQuantity(updatedItem, newQuantity)
                    },
                    onRemove = { itemToRemove ->
                        cartManager.removeFromCart(itemToRemove)
                    }
                )
            }
        }

        // Checkout Button fixed at bottom with safe padding and cost summary
        CheckoutButton(
            cartItems = cartItems,
            cartManager = cartManager,
            currencyCode = currencyCode,
            onCheckout = onCheckout
        )
    }
}

@Composable
private fun CartItemsSectionHeader(
    cartManager: CartManager
) {
    Text(
        text = "Items (${cartManager.itemCount})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 12.dp)
            .testTag("cart_items_header")
    )
}

@Composable
private fun CartItemRowView(
    cartItem: CartItem,
    currencyCode: String,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemove: (CartItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${cartItem.product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .testTag("cart_item_image_${cartItem.product.id}"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(cartItem.product.image.drawableRes),
                    contentDescription = cartItem.product.name,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Product Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    modifier = Modifier.testTag("cart_item_name_${cartItem.product.id}")
                )

                Text(
                    text = CurrencyFormatter.format(cartItem.product.price, currencyCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("cart_item_price_${cartItem.product.id}")
                )
            }

            // Quantity Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newQuantity = cartItem.quantity - 1
                            if (newQuantity <= 0) {
                                onRemove(cartItem)
                            } else {
                                onQuantityChange(cartItem, newQuantity)
                            }
                        },
                        modifier = Modifier.testTag("cart_item_decrease_${cartItem.product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease quantity",
                            tint = if (cartItem.quantity > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = cartItem.quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("cart_item_quantity_${cartItem.product.id}")
                    )

                    IconButton(
                        onClick = {
                            onQuantityChange(cartItem, cartItem.quantity + 1)
                        },
                        modifier = Modifier.testTag("cart_item_increase_${cartItem.product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase quantity",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AppTextButton(
                    text = "Remove",
                    onClick = { onRemove(cartItem) },
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("cart_item_remove_${cartItem.product.id}")
                )
            }
        }
    }
}

@Composable
private fun CheckoutButton(
    cartItems: List<CartItem>,
    cartManager: CartManager,
    currencyCode: String,
    onCheckout: () -> Unit
) {
    // Calculate subtotal based on the current cart items - use remember to cache
    val formattedSubtotal = remember(cartItems, currencyCode) {
        CurrencyFormatter.format(cartManager.subtotal, currencyCode)
    }

    Column {
        HorizontalDivider()
        // Subtotal row above the checkout button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("cart_subtotal_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtotal",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("cart_subtotal_label")
            )
            Text(
                text = formattedSubtotal,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("cart_subtotal_amount")
            )
        }
        AppButton(
            text = "Proceed to Checkout",
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("cart_checkout_button")
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewCartScreen() {
    SampleTheme {
        CartScreen()
    }
} 