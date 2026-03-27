package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.core.utils.toSafeAmount
import com.paydock.feature.googlepay.domain.model.integration.GooglePayResult
import com.paydock.feature.googlepay.domain.model.integration.GooglePayWidgetConfig
import com.paydock.feature.googlepay.presentation.GooglePayAppearanceDefaults
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.COUNTRY_CODE_LIST
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import com.paydock.feature.googlepay.domain.model.integration.GooglePayShippingAddressParameters

@Composable
fun GooglePayContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<GooglePayResult>) -> Unit,
    configViewModel: ConfigViewModel = hiltViewModel(),
) {
    val globalConfig by configViewModel.globalConfig.collectAsState()
    val cartManager = remember { CartManager.shared }
    val cartTotal = cartManager.totalPrice

    val shippingAddressParameters = GooglePayShippingAddressParameters(
        phoneNumberRequired = false,
        allowedCountryCodes = COUNTRY_CODE_LIST
    )
    GooglePayWidget(
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        config = GooglePayWidgetConfig(
            accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
            serviceId = BuildConfig.SERVICE_ID_GOOGLE_PAY,
            isReadyToPayRequest = PaymentsUtil.createIsReadyToPayRequest(),
            paymentRequest = PaymentsUtil.createGooglePayRequest(
                amount = cartTotal.toSafeAmount(),
                amountLabel = "Goodies",
                currencyCode = globalConfig.currencyCode,
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