package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.ClickToPayAppearanceDefaults
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun ClickToPayItem(context: Context, stylingViewModel: StylingViewModel) {
    // This is to ensure we hide the WebView once completed
    var hasCompletedFlow: Boolean by remember { mutableStateOf(false) }
    val clickToPayAppearance by stylingViewModel.clickToPayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        clickToPayAppearance ?: ClickToPayAppearanceDefaults.appearance()
    if (!hasCompletedFlow) {
        // Test Cards: https://developer.mastercard.com/unified-checkout-solutions/documentation/testing/test_cases/click_to_pay_case/#test-cards
        // Test Cards (MPGS): https://ap-gateway.mastercard.com/api/documentation/integrationGuidelines/supportedFeatures/testAndGoLive.html?locale=en_US
        ClickToPayWidget(
            modifier = Modifier
                .fillMaxWidth(),
            config = ClickToPayWidgetConfig(
                accessToken = BuildConfig.WIDGET_ACCESS_TOKEN,
                serviceId = BuildConfig.GATEWAY_ID_CLICK_TO_PAY,
                meta = ClickToPayMeta(
                    disableSummaryScreen = true
                )
            ),
            appearance = currentOrDefaultAppearance,
        ) { result ->
            result.onSuccess {
                Log.d("[ClickToPayWidget]", it)
                Toast.makeText(context, "ClickToPay Result returned [$it]", Toast.LENGTH_SHORT)
                    .show()
                hasCompletedFlow = true
            }.onFailure {
                val error = it.toError()
                Log.d("[ClickToPayWidget]", error.displayableMessage)
                Toast.makeText(
                    context,
                    "ClickToPay Result failed! [${error.displayableMessage}]",
                    Toast.LENGTH_SHORT
                ).show()
//                            hasCompletedFlow = true
            }
        }
    }
}