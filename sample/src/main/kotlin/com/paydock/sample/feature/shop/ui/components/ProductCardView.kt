package com.paydock.sample.feature.shop.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ProductCategory
import kotlinx.coroutines.delay

@Composable
fun ProductCardView(
    product: Product,
    cardHeight: Dp,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showingAddedAnimation by remember { mutableStateOf(false) }
    val cartManager = remember { CartManager.shared }
    val cartItemsState = cartManager.cartItems.collectAsState()
    val productQty = cartItemsState.value.firstOrNull { it.product.id == product.id }?.quantity ?: 0
    val haptic = LocalHapticFeedback.current
    var pulse by remember { mutableStateOf(false) }
    val badgeScale by animateFloatAsState(
        targetValue = if (pulse) 1.2f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "badgeScale"
    )
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
            // Top-right quantity badge
            if (productQty > 0) {
                Badge(
                    modifier = Modifier
                        .zIndex(1f)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .scale(badgeScale)
                ) {
                    Text(text = productQty.toString(), fontSize = 10.sp)
                }
            }

            Column(
                modifier = Modifier.padding(padding)
            ) {
            // Product Image Container - Fixed Height
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
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                    ) {
                        when (painter.state) {
                            is coil.compose.AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(imageHeight)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                            is coil.compose.AsyncImagePainter.State.Error -> {
                                // Neutral error placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(imageHeight)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                } else if (fallbackResId != null) {
                    Image(
                        painter = painterResource(fallbackResId),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                    text = if (showingAddedAnimation) "Added!" else "Add to Cart",
                onClick = {
                    onAddToCart()
                    showingAddedAnimation = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    pulse = true
                },
                enabled = !showingAddedAnimation,
                    height = buttonHeight,
                    leadingIcon = {
                    Icon(
                        imageVector = if (showingAddedAnimation) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Reset animation after delay
    LaunchedEffect(showingAddedAnimation) {
        if (showingAddedAnimation) {
            delay(1000)
            showingAddedAnimation = false
            pulse = false
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
                imageName = "demo_product_1",
                category = ProductCategory.CLOTHING,
                imageUrl = "https://picsum.photos/seed/denim/600/400"
            ),
            cardHeight = 280.dp,
            onAddToCart = {}
        )
    }
}