package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.PayPalAppearanceDefaults
import com.paydock.feature.paypal.checkout.presentation.PayPalWidget
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.config.ConfigViewModel

@Composable
fun PayPalContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    val configViewModel: ConfigViewModel = hiltViewModel()
    val globalConfig by configViewModel.globalConfig.collectAsState()

    PayPalWidget(
        modifier = modifier.fillMaxWidth(),
        config = PayPalWidgetConfig(
            accessToken = globalConfig.apiAccessToken,
            gatewayId = BuildConfig.SERVICE_ID_PAYPAL,
            requestShipping = false,
            fundingSource = PayPalWidgetConfig.PayPalFundingSource.PAY_LATER
        ),
        appearance = PayPalAppearanceDefaults.appearance(),
        enabled = enabled,
        tokenRequest = tokenHandler,
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}