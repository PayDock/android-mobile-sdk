package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.zip.domain.model.ZipResult
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.presentation.ZipWidget
import com.paydock.feature.zip.presentation.ZipWidgetAppearanceDefaults
import com.paydock.sample.BuildConfig
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
    val globalConfig by configViewModel.globalConfig.collectAsState()
    // Use actual cart total instead of configured amount
    val cartTotal = CartManager.shared.totalPrice

    ZipWidget(
        modifier = modifier,
        enabled = enabled,
        config = ZipWidgetConfig(
            accessToken = globalConfig.apiAccessToken,
            gatewayId = BuildConfig.SERVICE_ID_ZIP,
            amount = BigDecimal(cartTotal),
            currency = globalConfig.currencyCode,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com"
        ),
        appearance = ZipWidgetAppearanceDefaults.appearance().copy(
            buttonStyle = ZipWidgetAppearanceDefaults.appearance().buttonStyle
        ),
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}

