package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.zip.domain.model.ZipResult
import com.paydock.feature.zip.presentation.ZipWidget
import com.paydock.feature.zip.presentation.ZipWidgetAppearanceDefaults
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import java.math.BigDecimal

@Composable
fun ZipContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loadingDelegate: WidgetLoadingDelegate?,
    resultHandler: (Result<ZipResult>) -> Unit
) {
    val configViewModel: ConfigViewModel = hiltViewModel()
    val zipConfig by configViewModel.zipWidgetConfig.collectAsState()
    
    // Use actual cart total instead of configured amount
    val cartTotal = CartManager.shared.totalPrice
    val checkoutZipConfig = zipConfig.copy(
        amount = BigDecimal(cartTotal)
    )

    ZipWidget(
        modifier = modifier,
        enabled = enabled,
        config = checkoutZipConfig,
        appearance = ZipWidgetAppearanceDefaults.appearance().copy(
            buttonStyle = ZipWidgetAppearanceDefaults.appearance().buttonStyle
        ),
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}

