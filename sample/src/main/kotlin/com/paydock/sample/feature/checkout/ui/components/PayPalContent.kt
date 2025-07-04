package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.PayPalAppearanceDefaults
import com.paydock.feature.paypal.checkout.presentation.PayPalWidget
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun PayPalContent(
    stylingViewModel: StylingViewModel,
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    val paypalAppearance by stylingViewModel.paypalWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = paypalAppearance ?: PayPalAppearanceDefaults.appearance()
    PayPalWidget(
        modifier = Modifier.fillMaxWidth(),
        config = PayPalWidgetConfig(
            requestShipping = false
        ),
        appearance = currentOrDefaultAppearance,
        enabled = enabled,
        tokenRequest = tokenHandler,
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}