package com.paydock.sample.feature.shop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ProductCategory
import com.paydock.sample.feature.shop.domain.model.ProductImage

@Composable
fun ProductCardView(
    product: Product,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartManager = remember { CartManager.shared }
    val cartItemsState = cartManager.cartItems.collectAsState()
    val productQty = cartItemsState.value.firstOrNull { it.product.id == product.id }?.quantity ?: 0
    val haptic = LocalHapticFeedback.current
    val buttonText = if (productQty > 0) "$productQty in Cart" else "Add to Cart"

    // Define spacing
    val padding = 12.dp
    val spacing = 8.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            // Product Image Container - Fixed height for consistency across all cards
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Square image, centered and sized to fit within the container
                Image(
                    painter = painterResource(product.image.drawableRes),
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing))

            // Product Name - Flexible height, respects font scaling
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing))

            // Product Description - Flexible height, respects font scaling
            Text(
                text = product.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing))

            // Price - Flexible height, respects font scaling
            Text(
                text = product.formattedPrice,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Flexible spacer - pushes button to bottom of card
            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(spacing))

            // Add to Cart Button - Docked to bottom, wraps content height naturally with font scaling
            AppButton(
                text = buttonText,
                onClick = {
                    onAddToCart()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                enabled = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewProductCardView() {
    SampleTheme {
        ProductCardView(
            product = Product(
                id = "1",
                name = "Classic Denim Jacket",
                description = "Premium denim jacket with classic fit and vintage styling",
                price = 89.99,
                image = ProductImage.JACKET,
                category = ProductCategory.CLOTHING
            ),
            onAddToCart = {}
        )
    }
}