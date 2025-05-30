package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.colespay.presentation.ColesPayWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ColesPayContent(
    tokenHandler: (onTokenReceived: (String) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<String>) -> Unit,
) {
    ColesPayWidget(
        modifier = Modifier.fillMaxWidth(),
        clientId = BuildConfig.COLES_PAY_CLIENT_ID,
        token = tokenHandler,
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}

@Composable
@Preview
private fun ColesPayContentDefault() {
    SampleTheme {
        ColesPayContent({}, null, {})
    }
}