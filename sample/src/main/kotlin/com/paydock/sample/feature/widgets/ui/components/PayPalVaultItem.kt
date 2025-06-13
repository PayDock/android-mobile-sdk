package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.PayPalPaymentSourceAppearanceDefaults
import com.paydock.feature.paypal.vault.presentation.PayPalSavePaymentSourceWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun PayPalVaultItem(context: Context, stylingViewModel: StylingViewModel) {
    val paypalVaultAppearance by stylingViewModel.paypalVaultWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        paypalVaultAppearance ?: PayPalPaymentSourceAppearanceDefaults.appearance()
    PayPalSavePaymentSourceWidget(
        modifier = Modifier.padding(16.dp),
        config = PayPalVaultConfig(
            accessToken = BuildConfig.WIDGET_ACCESS_TOKEN,
            gatewayId = BuildConfig.GATEWAY_ID_PAY_PAL
        ),
        appearance = currentOrDefaultAppearance
    ) { result ->
        result.onSuccess {
            Log.d("[PayPalSavePaymentSourceWidget]", "Success: $it")
            Toast.makeText(context, "PayPal Vault Result returned [$it]", Toast.LENGTH_SHORT).show()
        }.onFailure {
            val error = it.toError()
            Log.d("[PayPalSavePaymentSourceWidget]", "Failure: ${error.displayableMessage}")
            Toast.makeText(
                context,
                "PayPal Vault Result failed! [${error.displayableMessage}]",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}