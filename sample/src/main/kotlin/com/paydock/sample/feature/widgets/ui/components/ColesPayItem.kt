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
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.ColesPayWidget
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearanceDefaults
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.wallet.presentation.WalletViewModel

@Composable
fun ColesPayItem(
    context: Context,
    walletViewModel: WalletViewModel = hiltViewModel(),
    stylingViewModel: StylingViewModel
) {
    val colesPayAppearance by stylingViewModel.colesPayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        colesPayAppearance ?: ColesPayWidgetAppearanceDefaults.appearance()
    val uiState by walletViewModel.stateFlow.collectAsState()
    ColesPayWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        config = ColesPayWidgetConfig(BuildConfig.WALLET_ID_COLES_PAY),
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[ColesPayWidget Event]", "[type=${event.type}] $event")
            }
        },
        appearance = currentOrDefaultAppearance,
        tokenRequest = walletViewModel.getWalletTokenResultCallback(WalletType.COLES_PAY)
    ) { result ->
        result.onSuccess {
            Log.d("[ColesPayWidget]", "Success: $it")
            Toast.makeText(context, "Coles Pay Result returned [$it]", Toast.LENGTH_SHORT).show()
        }.onFailure {
            val error = it.toError()
            Log.d("[ColesPayWidget]", "Failure: ${error.displayableMessage}")
            Toast.makeText(
                context,
                "Coles Pay Result failed! [${error.displayableMessage}]",
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