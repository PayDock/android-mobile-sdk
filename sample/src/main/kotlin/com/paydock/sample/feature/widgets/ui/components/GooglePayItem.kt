package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import com.paydock.feature.googlepay.presentation.GooglePayAppearanceDefaults
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.wallet.presentation.WalletViewModel

@Composable
fun GooglePayItem(
    context: Context,
    walletViewModel: WalletViewModel = hiltViewModel(),
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel
) {
    val uiState by walletViewModel.stateFlow.collectAsState()
    val googlePayAppearance by stylingViewModel.googlePayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = googlePayAppearance ?: GooglePayAppearanceDefaults.appearance()
    val googlePayCardConfig by configViewModel.googlePayWidgetConfig.collectAsState()

    GooglePayWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        config = googlePayCardConfig,
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[GooglePayWidget Event]", "[type=${event.type}] $event")
            }
        },
        appearance = currentOrDefaultAppearance,
        completion = { result ->
            result.onSuccess { result ->
                Log.d("[GooglePayWidget]", "Success: $result")
                Toast.makeText(
                    context,
                    "Google Pay Token created: ${result.token}",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure {
                val error = it.toError()
                Log.d("[GooglePayWidget]", "Failure: ${error.displayableMessage}")
                Toast.makeText(
                    context,
                    "Google Pay Result failed! [${error.displayableMessage}]",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    ) {
        Button(
            onClick = { /* No effect on click as requested */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fallback Button")
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
