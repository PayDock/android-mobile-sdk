package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.googlepay.domain.model.GooglePayWidgetConfig
import com.paydock.feature.googlepay.presentation.GooglePayAppearanceDefaults
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.COUNTRY_CODE_LIST
import com.paydock.sample.core.MERCHANT_NAME
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Composable
fun GooglePayContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    val shippingAddressParameters = JSONObject().apply {
        put("phoneNumberRequired", false)
        put("allowedCountryCodes", JSONArray(COUNTRY_CODE_LIST))
    }
    GooglePayWidget(
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        tokenRequest = tokenHandler,
        config = GooglePayWidgetConfig(
            isReadyToPayRequest = PaymentsUtil.createIsReadyToPayRequest(),
            paymentRequest = PaymentsUtil.createGooglePayRequest(
                amount = BigDecimal(AMOUNT),
                amountLabel = "Goodies",
                currencyCode = AU_CURRENCY_CODE,
                countryCode = AU_COUNTRY_CODE,
                merchantName = MERCHANT_NAME,
                merchantIdentifier = BuildConfig.MERCHANT_ID_GOOGLE_PAY,
                shippingAddressRequired = true,
                shippingAddressParameters = shippingAddressParameters
            )
        ),
        loadingDelegate = loadingDelegate,
        appearance = GooglePayAppearanceDefaults.appearance(),
        completion = resultHandler
    )
}