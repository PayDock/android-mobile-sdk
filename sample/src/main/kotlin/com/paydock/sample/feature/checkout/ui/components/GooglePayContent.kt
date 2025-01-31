package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.COUNTRY_CODE_LIST
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.designsystems.theme.SampleTheme
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Composable
fun GooglePayContent(
    tokenHandler: (onTokenReceived: (String) -> Unit) -> Unit,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    val shippingAddressParameters = JSONObject().apply {
        put("phoneNumberRequired", false)
        put("allowedCountryCodes", JSONArray(COUNTRY_CODE_LIST))
    }
    GooglePayWidget(
        modifier = Modifier.fillMaxWidth(),
        token = tokenHandler,
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
        ),
        completion = resultHandler
    )
}

@Composable
@Preview
private fun GooglePayContentDefault() {
    SampleTheme {
        GooglePayContent({}, {})
    }
}