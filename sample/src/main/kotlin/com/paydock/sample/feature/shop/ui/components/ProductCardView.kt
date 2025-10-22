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
import androidx.compose.ui.unit.Dp
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
    cardHeight: Dp,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartManager = remember { CartManager.shared }
    val cartItemsState = cartManager.cartItems.collectAsState()
    val productQty = cartItemsState.value.firstOrNull { it.product.id == product.id }?.quantity ?: 0
    val haptic = LocalHapticFeedback.current
    val buttonText = if (productQty > 0) "${productQty} in Cart" else "Add to Cart"
    // Define fixed heights for consistent layout
    val padding = 12.dp
    val titleHeight = 44.dp // 2 lines at 16pt + spacing
    val descriptionHeight = 32.dp // 2 lines at 12pt + spacing
    val priceHeight = 24.dp // Single line at 18pt
    val buttonHeight = 36.dp
    val spacing = 8.dp
    
    // Calculate image height based on remaining space
    val contentHeight = cardHeight - (padding * 2)
    val fixedContentHeight = titleHeight + descriptionHeight + priceHeight + buttonHeight + (spacing * 4)
    val imageHeight = contentHeight - fixedContentHeight
    
    Card(
        modifier = modifier
            .height(cardHeight)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Column(
                modifier = Modifier.padding(padding)
            ) {
            // Product Image Container - Fixed Height with centered square image
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Square image, centered and sized to fit within the container
                val imageSize = minOf(imageHeight, 160.dp)
                Image(
                    painter = painterResource(product.image.drawableRes),
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(imageSize)
                )
            }
            
            Spacer(modifier = Modifier.height(spacing))

            // Product Name - Fixed Height Container
            Box(
                modifier = Modifier.height(titleHeight),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(spacing))
            
            // Product Description - Fixed Height Container
            Box(
                modifier = Modifier.height(descriptionHeight),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(spacing))
            
            // Price - Fixed Height Container
            Box(
                modifier = Modifier.height(priceHeight),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = product.formattedPrice,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(spacing))

                // Add to Cart Button - Fixed Height with quantity badge
                AppButton(
                    text = buttonText,
                    onClick = {
                        onAddToCart()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    enabled = true,
                    height = buttonHeight,
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
            cardHeight = 280.dp,
            onAddToCart = {}
        )
    }
}