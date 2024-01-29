/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.COUNTRY_CODE_LIST
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.checkout.CheckoutViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Composable
fun GooglePayContent(viewModel: CheckoutViewModel) {
    val shippingAddressParameters = JSONObject().apply {
        put("phoneNumberRequired", false)
        put("allowedCountryCodes", JSONArray(COUNTRY_CODE_LIST))
    }
    GooglePayWidget(
        modifier = Modifier.fillMaxWidth(),
        token = viewModel.getWalletToken(
            currencyCode = AU_CURRENCY_CODE,
            walletType = "google",
            gatewayId = BuildConfig.GATEWAY_ID
        ),
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
        completion = viewModel::handleChargeResult
    )
}

@Composable
@Preview
private fun GooglePayContentDefault() {
    SampleTheme {
        GooglePayContent(
            viewModel = hiltViewModel()
        )
    }
}