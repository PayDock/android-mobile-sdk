package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.feature.flypay.domain.model.integration.FlyPayWidgetConfig
import com.paydock.feature.flypay.presentation.FlyPayWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun FlyPayContent(
    tokenHandler: (onTokenReceived: (String) -> Unit) -> Unit,
    resultHandler: (Result<String>) -> Unit,
) {
    FlyPayWidget(
        modifier = Modifier.fillMaxWidth(),
        config = FlyPayWidgetConfig(clientId = BuildConfig.FLY_PAY_CLIENT_ID),
        token = tokenHandler,
        completion = resultHandler
    )
}

@Composable
@PreviewLightDark
private fun FlyPayContentDefault() {
    SampleTheme {
        FlyPayContent({}, {})
    }
}