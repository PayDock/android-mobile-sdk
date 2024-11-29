package com.paydock.sample.feature.account.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.PayPalSavePaymentSourceWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun AccountScreen() {
    val scrollState = rememberScrollState()
    val delegate = remember { AccountLoadingDelegate() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            color = Theme.colors.onSurface,
            style = Theme.typography.sectionHeader,
            text = stringResource(R.string.label_saved_payment_method),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp
                )
            ) {
                Image(
                    modifier = Modifier
                        .width(68.dp)
                        .height(18.dp),
                    painter = painterResource(id = R.drawable.ic_paypal),
                    contentDescription = "PayPal logo"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(bottom = 24.dp),
                    style = Theme.typography.cardDescription,
                    text = stringResource(R.string.label_link_paypal_desc)
                )
                PayPalSavePaymentSourceWidget(
                    config = getPayPalVaultConfig(),
                    loadingDelegate = delegate
                ) { result ->
                    result.onSuccess {
                        Log.d("[PayPalSavePaymentSourceWidget]", "Success: $it")
                    }.onFailure {
                        val error = it.toError()
                        Log.d(
                            "[PayPalSavePaymentSourceWidget]", "Failure: ${error.displayableMessage}"
                        )
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Theme.colors.outlineVariant)
    }
}

private fun getPayPalVaultConfig(): PayPalVaultConfig {
    return PayPalVaultConfig(
        accessToken = BuildConfig.ACCESS_TOKEN,
        gatewayId = BuildConfig.GATEWAY_ID_PAY_PAL
    )
}

class AccountLoadingDelegate(): WidgetLoadingDelegate {
    override fun widgetLoadingDidStart() {
        Log.d("[PayPalSavePaymentSourceWidget]", "Loading: true")
    }

    override fun widgetLoadingDidFinish() {
        Log.d("[PayPalSavePaymentSourceWidget]", "Loading: false")
    }
}