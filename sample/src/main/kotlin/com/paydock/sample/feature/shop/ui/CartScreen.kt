package com.paydock.sample.feature.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonShape
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.domain.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit = {},
    onContinueShopping: () -> Unit = {}
) {
    val cartManager = remember { CartManager.shared }
    val cartItems by cartManager.cartItems.collectAsState()

    if (cartManager.isEmpty) {
        EmptyCartView(onContinueShopping = onContinueShopping)
    } else {
        CartContentView(
            cartManager = cartManager,
            cartItems = cartItems,
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
            .padding(32.dp),
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
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Add some products to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AppButton(
                text = "Continue Shopping",
                onClick = onContinueShopping,
                shape = AppButtonShape.Pill,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
private fun CartContentView(
    cartManager: CartManager,
    cartItems: List<CartItem>,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cart Items Section
            CartItemsSection(
                cartItems = cartItems,
                cartManager = cartManager
            )

            // Gift Card, Shipping, and Order Summary moved to Payment step
        }

        // Checkout Button fixed at bottom with safe padding
        CheckoutButton(onCheckout = onCheckout)
    }
}

@Composable
private fun CartItemsSection(
    cartItems: List<CartItem>,
    cartManager: CartManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Items (${cartManager.itemCount})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        cartItems.forEach { cartItem ->
            CartItemRowView(
                cartItem = cartItem,
                onQuantityChange = { updatedItem, newQuantity ->
                    cartManager.updateQuantity(updatedItem, newQuantity)
                },
                onRemove = { itemToRemove ->
                    cartManager.removeFromCart(itemToRemove)
                }
            )
        }
    }
}

@Composable
private fun CartItemRowView(
    cartItem: CartItem,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemove: (CartItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            run {
                val product = cartItem.product
                val fallbackResId = when (product.imageName) {
                    "demo_product_1" -> R.drawable.demo_product_1
                    "demo_product_2" -> R.drawable.demo_product_2
                    "ic_coles_pay" -> R.drawable.ic_coles_pay
                    "ic_afterpay" -> R.drawable.ic_afterpay
                    "ic_paypal" -> R.drawable.ic_paypal
                    "ic_google_pay_default" -> R.drawable.ic_google_pay_default
                    "ic_src" -> R.drawable.ic_src
                    "ic_launcher_foreground" -> R.drawable.ic_launcher_foreground
                    "demo_icon" -> R.drawable.demo_icon
                    "ic_widgets" -> R.drawable.ic_widgets
                    "ic_style" -> R.drawable.ic_style
                    "ic_checkout" -> R.drawable.ic_checkout
                    else -> null
                }
                val context = LocalContext.current
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        placeholder = fallbackResId?.let { painterResource(it) },
                        error = fallbackResId?.let { painterResource(it) }
                            ?: painterResource(R.drawable.demo_product_1),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    )
                } else if (fallbackResId != null) {
                    Image(
                        painter = painterResource(fallbackResId),
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
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
                    maxLines = 2
                )
                
                Text(
                    text = cartItem.product.formattedPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        }
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
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(
                        onClick = {
                            onQuantityChange(cartItem, cartItem.quantity + 1)
                        }
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
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CheckoutButton(
    onCheckout: () -> Unit
) {
    Column {
        HorizontalDivider()
        AppButton(
            text = "Proceed to Checkout",
            onClick = onCheckout,
            height = 50.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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