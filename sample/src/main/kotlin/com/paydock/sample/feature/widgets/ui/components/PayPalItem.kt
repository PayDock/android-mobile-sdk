package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.domain.model.Event
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.paypal.checkout.presentation.PayPalAppearanceDefaults
import com.paydock.feature.paypal.checkout.presentation.PayPalWidget
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.wallet.presentation.WalletViewModel

@Composable
fun PayPalItem(
    context: Context,
    walletViewModel: WalletViewModel = hiltViewModel(),
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel
) {
    val uiState by walletViewModel.stateFlow.collectAsState()
    val paypalAppearance by stylingViewModel.paypalWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = paypalAppearance ?: PayPalAppearanceDefaults.appearance()
    val paypalConfig by configViewModel.paypalWidgetConfig.collectAsState()

    PayPalWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        config = paypalConfig,
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[PayPalWidget Event]", "[type=${event.type}] $event")
            }
        },
        tokenRequest = walletViewModel.getWalletTokenResultCallback(WalletType.PAY_PAL),
        appearance = currentOrDefaultAppearance
    ) { result ->
        result.onSuccess {
            Log.d("[PayPalWidget]", "Success: $it")
            Toast.makeText(context, "PayPal Result returned [$it]", Toast.LENGTH_SHORT).show()
        }.onFailure {
            val error = it.toError()
            Log.d("[PayPalWidget]", "Failure: ${error.displayableMessage}")
            Toast.makeText(
                context,
                "PayPal Result failed! [${error.displayableMessage}]",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    when {
        !uiState.error.isNullOrBlank() -> {
            Toast.makeText(context, uiState.error ?: CHARGE_TRANSACTION_ERROR, Toast.LENGTH_SHORT)
                .show()
        }

        uiState.isLoading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}