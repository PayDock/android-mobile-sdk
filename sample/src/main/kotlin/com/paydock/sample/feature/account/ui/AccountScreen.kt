package com.paydock.sample.feature.account.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.PayPalSavePaymentSourceWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.dialogs.ErrorDialog
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.feature.account.AccountViewModel

@Composable
fun AccountScreen(viewModel: AccountViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uiState by viewModel.stateFlow.collectAsState()

    LaunchedEffect(uiState.customer) {
        uiState.customer?.let {
            Toast.makeText(
                context,
                context.getString(R.string.desc_pay_pal_customer_created),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetResultState()
        }
    }

    if (!uiState.error.isNullOrBlank()) {
        ErrorDialog(
            onDismissRequest = { viewModel.resetResultState() },
            onConfirmation = {
                viewModel.resetResultState()
            },
            dialogText = uiState.error!!,
        )
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp)
            .verticalScroll(scrollState)
    ) {
        Column {
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
                        enabled = !uiState.isLoading,
                        config = getPayPalVaultConfig(),
                        loadingDelegate = viewModel
                    ) { result ->
                        result.onSuccess { vaultResult ->
                            viewModel.createCustomer(vaultResult.token)
                        }.onFailure {
                            val error = it.toError()
                            Toast.makeText(context, error.displayableMessage, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                color = Theme.colors.outlineVariant
            )
        }
    }

    if (uiState.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.Center)
        )
    }
}

private fun getPayPalVaultConfig(): PayPalVaultConfig {
    return PayPalVaultConfig(
        accessToken = BuildConfig.ACCESS_TOKEN,
        gatewayId = BuildConfig.GATEWAY_ID_PAY_PAL
    )
}