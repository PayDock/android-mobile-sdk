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
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.feature.wallet.presentation.WalletViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Composable
fun GooglePayItem(context: Context, walletViewModel: WalletViewModel = hiltViewModel()) {
    val uiState by walletViewModel.stateFlow.collectAsState()
    val shippingAddressParameters = JSONObject().apply {
        put("phoneNumberRequired", false)
        put("allowedCountryCodes", JSONArray(listOf("US", "GB", "AU")))
    }
    GooglePayWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        token = walletViewModel.getWalletToken(WalletType.GOOGLE),
        isReadyToPayRequest = PaymentsUtil.createIsReadyToPayRequest(),
        paymentRequest = PaymentsUtil.createGooglePayRequest(
            amount = BigDecimal(AMOUNT),
            amountLabel = "Goodies",
            currencyCode = AU_CURRENCY_CODE,
            countryCode = AU_COUNTRY_CODE,
            merchantName = MERCHANT_NAME,
            merchantIdentifier = BuildConfig.MERCHANT_IDENTIFIER,
            shippingAddressRequired = true,
            shippingAddressParameters = shippingAddressParameters
        )
    ) { result ->
        result.onSuccess {
            Log.d("[GooglePayWidget]", "Success: $it")
            Toast.makeText(context, "Google Pay Result returned [$it]", Toast.LENGTH_SHORT).show()
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
