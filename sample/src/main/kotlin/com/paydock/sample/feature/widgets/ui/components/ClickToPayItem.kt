package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.sample.BuildConfig

@Composable
fun ClickToPayItem(context: Context, accessToken: String) {
    // This is to ensure we hide the WebView once completed
    var hasCompletedFlow: Boolean by remember { mutableStateOf(false) }
    if (!hasCompletedFlow) {
        // Test Cards: https://developer.mastercard.com/unified-checkout-solutions/documentation/testing/test_cases/click_to_pay_case/#test-cards
        ClickToPayWidget(
            modifier = Modifier
                .fillMaxWidth(),
            accessToken = accessToken,
            serviceId = BuildConfig.GATEWAY_ID_MASTERCARD_SRC,
            meta = ClickToPayMeta(
                disableSummaryScreen = true
            )
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