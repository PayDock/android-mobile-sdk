package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.ColesPayWidget
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearanceDefaults
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.BuildConfig

@Composable
fun ColesPayContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<String>) -> Unit,
) {
    ColesPayWidget(
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        config = ColesPayWidgetConfig(BuildConfig.WALLET_ID_COLES_PAY),
        appearance = ColesPayWidgetAppearanceDefaults.appearance(),
        tokenRequest = tokenHandler,
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}