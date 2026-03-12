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
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.AfterpayAppearanceDefaults
import com.paydock.feature.afterpay.presentation.AfterpayWidget
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.wallet.presentation.WalletViewModel
import java.util.Currency
import java.util.Locale

@Composable
fun AfterpayItem(
    context: Context,
    walletViewModel: WalletViewModel = hiltViewModel(),
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel
) {
    val uiState by walletViewModel.stateFlow.collectAsState()
    val afterpayConfig by configViewModel.afterpayWidgetConfig.collectAsState()
    val afterpayAppearance by stylingViewModel.afterpayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = afterpayAppearance ?: AfterpayAppearanceDefaults.appearance()
    AfterpayWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        tokenRequest = walletViewModel.getWalletTokenResultCallback(WalletType.AFTER_PAY),
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[AfterpayWidget Event]", "[type=${event.type}] $event")
            }
        },
        config = afterpayConfig,
        selectAddress = { _, provideShippingOptions ->
            val locale = afterpayConfig.locale ?: Locale.getDefault()
            val currency = Currency.getInstance(locale)
            val shippingOptions = listOf(
                AfterpayShippingOption(
                    "standard",
                    "Standard",
                    "",
                    currency,
                    "0.00".toBigDecimal(),
                    "50.00".toBigDecimal(),
                    "0.00".toBigDecimal(),
                ),
                AfterpayShippingOption(
                    "priority",
                    "Priority",
                    "Next business day",
                    currency,
                    "10.00".toBigDecimal(),
                    "60.00".toBigDecimal(),
                    null,
                )
            )
            provideShippingOptions(shippingOptions)
        },
        selectShippingOption = { shippingOption, provideShippingOptionUpdateResult ->
            val locale = afterpayConfig.locale ?: Locale.getDefault()
            val currency = Currency.getInstance(locale)
            // if standard shipping was selected, update the amounts
            // otherwise leave as is by passing null
            val result: AfterpayShippingOptionUpdate? =
                if (shippingOption.id == "standard") {
                    AfterpayShippingOptionUpdate(
                        "standard",
                        currency,
                        "0.00".toBigDecimal(),
                        "50.00".toBigDecimal(),
                        "2.00".toBigDecimal(),
                    )
                } else {
                    null
                }
            provideShippingOptionUpdateResult(result)
        },
        appearance = currentOrDefaultAppearance,
        enabled = !uiState.isLoading
    ) { result ->
        result.onSuccess {
            Log.d("[AfterpayWidget]", "Success: $it")
            Toast.makeText(context, "Afterpay Result returned [$it]", Toast.LENGTH_SHORT).show()
        }.onFailure {
            val error = it.toError()
            Log.d("[AfterpayWidget]", "Failure: ${error.displayableMessage}")
            Toast.makeText(
                context,
                "Afterpay Result failed! [${error.displayableMessage}]",
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