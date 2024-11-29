package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.paypal.checkout.presentation.PayPalWidget
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.feature.wallet.presentation.WalletViewModel

@Composable
fun PayPalItem(context: Context, walletViewModel: WalletViewModel = hiltViewModel()) {
    val uiState by walletViewModel.stateFlow.collectAsState()
    PayPalWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        token = walletViewModel.getWalletToken(WalletType.PAY_PAL),
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
    }
}