package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.ColesPayWidget
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearanceDefaults
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun ColesPayContent(
    stylingViewModel: StylingViewModel,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<String>) -> Unit,
) {
    val colesPayAppearance by stylingViewModel.colesPayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        colesPayAppearance ?: ColesPayWidgetAppearanceDefaults.appearance()
    ColesPayWidget(
        modifier = Modifier.fillMaxWidth(),
        config = ColesPayWidgetConfig(BuildConfig.COLES_PAY_CLIENT_ID),
        appearance = currentOrDefaultAppearance,
        tokenRequest = tokenHandler,
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}