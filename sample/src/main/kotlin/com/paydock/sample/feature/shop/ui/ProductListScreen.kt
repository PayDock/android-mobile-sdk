package com.paydock.sample.feature.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.data.ProductService
import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ProductCategory
import com.paydock.sample.feature.shop.ui.components.CategoryFilter
import com.paydock.sample.feature.shop.ui.components.ProductCardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val cartManager = remember { CartManager.shared }
    val productService = remember { ProductService.shared }
    
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    
    val cartItemCount by cartManager.cartItems.collectAsState()
    val itemCount = cartItemCount.sumOf { it.quantity }
    
    // Fixed card height for consistent appearance
    val cardHeight = 280.dp
    
    LaunchedEffect(Unit) {
        products = productService.getAllProducts()
    }
    
    val filteredProducts = remember(products, selectedCategory) {
        selectedCategory?.let { category ->
            products.filter { it.category == category }
        } ?: products
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category Filter
            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height((cardHeight + 16.dp) * (kotlin.math.ceil(filteredProducts.size / 2.0).toInt()))
            ) {
                items(filteredProducts) { product ->
                    ProductCardView(
                        product = product,
                        cardHeight = cardHeight,
                        onAddToCart = {
                            cartManager.addToCart(product)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
    }
}

@PreviewLightDark
@Composable
internal fun PreviewProductListScreen() {
    SampleTheme {
        ProductListScreen()
    }
} 