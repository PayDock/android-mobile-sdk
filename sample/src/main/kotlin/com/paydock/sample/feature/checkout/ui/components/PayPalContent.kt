package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.paypal.checkout.domain.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.PayPalWidget
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun PayPalContent(
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (String) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    PayPalWidget(
        modifier = Modifier.fillMaxWidth(),
        config = PayPalWidgetConfig(
            requestShipping = false
        ),
        enabled = enabled,
        token = tokenHandler,
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}

@Composable
@PreviewLightDark
private fun PayPalContentDefault() {
    SampleTheme {
        PayPalContent(true, {}, null, {})
    }
}