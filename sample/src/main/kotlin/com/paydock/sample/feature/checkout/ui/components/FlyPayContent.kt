package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
        clientId = BuildConfig.FLY_PAY_CLIENT_ID,
        token = tokenHandler,
        completion = resultHandler
    )
}

@Composable
@Preview
private fun FlyPayContentDefault() {
    SampleTheme {
        FlyPayContent({}, {})
    }
}