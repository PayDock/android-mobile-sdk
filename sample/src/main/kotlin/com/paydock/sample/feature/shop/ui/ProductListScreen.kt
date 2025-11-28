package com.paydock.sample.feature.shop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.sample.feature.shop.data.ProductService
import com.paydock.sample.feature.shop.domain.model.Product
import com.paydock.sample.feature.shop.domain.model.ProductCategory
import com.paydock.sample.feature.shop.ui.components.CategoryFilter
import com.paydock.sample.feature.shop.ui.components.ProductCardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen() {
    val cartManager = remember { CartManager.shared }
    val productService = remember { ProductService.shared }
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }

    LaunchedEffect(Unit) {
        products = productService.getAllProducts()
    }

    val filteredProducts = remember(products, selectedCategory) {
        selectedCategory?.let { category ->
            products.filter { it.category == category }
        } ?: products
    }

    // Calculate a fixed card height that scales with font size
    // Base: 320dp for normal font, scales with fontScale to accommodate multi-line content
    // At 2x font scale: 320 + (120 * 1.0) = 440dp
    // At 3x font scale: 320 + (120 * 2.0) = 560dp
    val cardHeight = remember(fontScale) {
        (320.dp + (120.dp * (fontScale - 1f))).coerceAtLeast(320.dp)
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

        // Products Grid with uniform card heights
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false,
            modifier = Modifier.height(
                (filteredProducts.size / 2.0 + 0.5).toInt().times(cardHeight + 16.dp)
            )
        ) {
            items(
                items = filteredProducts,
                key = { product -> product.id }
            ) { product ->
                ProductCardView(
                    product = product,
                    onAddToCart = {
                        cartManager.addToCart(product)
                    },
                    modifier = Modifier.height(cardHeight)
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