/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

package com.paydock.sample.feature.widgets.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.core.presentation.ui.extensions.toast
import com.paydock.feature.address.presentation.AddressDetailsWidget
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.feature.flypay.presentation.FlyPayWidget
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.paypal.presentation.PayPalWidget
import com.paydock.feature.threeDS.presentation.ThreeDSWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.core.US_CURRENCY_CODE
import com.paydock.sample.feature.card.CardViewModel
import com.paydock.sample.feature.threeDS.ThreeDSViewModel
import com.paydock.sample.feature.wallet.WalletViewModel
import com.paydock.sample.feature.widgets.ui.models.WidgetType
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Composable
fun WidgetInfoScreen(
    widgetType: WidgetType,
    walletViewModel: WalletViewModel = hiltViewModel(),
    cardViewModel: CardViewModel = hiltViewModel(),
    threeDSViewModel: ThreeDSViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        when (widgetType) {
            WidgetType.CREDIT_CARD_DETAILS -> {
                CardDetailsWidget(
                    modifier = Modifier.padding(16.dp),
                    gatewayId = BuildConfig.GATEWAY_ID, completion = { result ->
                        result.onSuccess {
                            context.toast("Tokenised card was successful! [$it]")
                        }.onFailure {
                            context.toast("Tokenised card failed! [$it]")
                        }
                    })
            }

            WidgetType.GIFT_CARD_DETAILS -> {
                GiftCardWidget(
                    storePin = true, completion = { result ->
                        result.onSuccess {
                            context.toast("Tokenised card was successful! [$it]")
                        }.onFailure {
                            context.toast("Tokenised card failed! [$it]")
                        }
                    })
            }

            WidgetType.ADDRESS_DETAILS -> {
                AddressDetailsWidget(modifier = Modifier.padding(16.dp)) { result ->
                    context.toast("Address details returned [$result]")
                }
            }

            WidgetType.GOOGLE_PAY -> {
                val uiState by walletViewModel.stateFlow.collectAsState()
                val shippingAddressParameters = JSONObject().apply {
                    put("phoneNumberRequired", false)
                    put("allowedCountryCodes", JSONArray(listOf("US", "GB", "AU")))
                }
                GooglePayWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    token = walletViewModel.getWalletToken(
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
                    )
                ) { result ->
                    result.onSuccess {
                        context.toast("Google Pay Result returned [$it]")
                    }.onFailure {
                        context.toast("Google Pay Result failed! [$it]")
                    }
                }
                when {
                    !uiState.error.isNullOrBlank() -> {
                        context.toast(uiState.error ?: CHARGE_TRANSACTION_ERROR)
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

            WidgetType.PAY_PAL -> {
                val uiState by walletViewModel.stateFlow.collectAsState()
                PayPalWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    token = walletViewModel.getWalletToken(
                        currencyCode = US_CURRENCY_CODE,
                        walletType = "paypal",
                        gatewayId = BuildConfig.GATEWAY_ID_PAY_PAL
                    )
                ) { result ->
                    result.onSuccess {
                        context.toast("PayPal Result returned [$it]")
                    }.onFailure {
                        context.toast("PayPal Result failed! [$it]")
                    }
                }
                when {
                    !uiState.error.isNullOrBlank() -> {
                        context.toast(uiState.error ?: CHARGE_TRANSACTION_ERROR)
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

            WidgetType.FLY_PAY -> {
                val uiState by walletViewModel.stateFlow.collectAsState()
                FlyPayWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    token = walletViewModel.getWalletToken(
                        manualCapture = true,
                        currencyCode = AU_CURRENCY_CODE,
                        walletType = "flypay",
                        gatewayId = BuildConfig.GATEWAY_ID_FLY_PAY
                    )
                ) { result ->
                    result.onSuccess {
                        context.toast("FlyPay Result returned [$it]")
                    }.onFailure {
                        context.toast("FlyPay Result failed! [$it]")
                    }
                }
                when {
                    !uiState.error.isNullOrBlank() -> {
                        context.toast(uiState.error ?: CHARGE_TRANSACTION_ERROR)
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

            WidgetType.INTEGRATED_3DS -> {
                val cardUIState by cardViewModel.stateFlow.collectAsState()
                val threeDSUIState by threeDSViewModel.stateFlow.collectAsState()
                // Use LaunchedEffect to execute the API request and state collection only once
                LaunchedEffect(cardViewModel) {
                    cardViewModel.resetResultState()
                    cardViewModel.tokeniseCardDetails()
                }
                LaunchedEffect(threeDSViewModel) {
                    threeDSViewModel.resetResultState()
                }
                val cardToken = cardUIState.token
                val threeDSToken = threeDSUIState.token
                when {
                    !threeDSToken.isNullOrBlank() -> {
                        ThreeDSWidget(token = threeDSToken) { result ->
                            result.onSuccess {
                                context.toast("3DS Result returned [$it]")
                            }.onFailure {
                                context.toast("3DS Result failed! [$it]")
                            }
                        }
                    }

                    !cardToken.isNullOrBlank() -> {
                        cardViewModel.resetResultState()
                        threeDSViewModel.createIntegrated3dsToken(cardToken)
                    }

                    !cardUIState.error.isNullOrBlank() -> {
                        context.toast(cardUIState.error ?: TOKENISE_CARD_ERROR)
                    }

                    !threeDSUIState.error.isNullOrBlank() -> {
                        context.toast(threeDSUIState.error ?: THREE_DS_CARD_ERROR)
                    }

                    cardUIState.isLoading || threeDSUIState.isLoading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            WidgetType.STANDALONE_3DS -> {
                val cardUIState by cardViewModel.stateFlow.collectAsState()
                val threeDSUIState by threeDSViewModel.stateFlow.collectAsState()
                // Use LaunchedEffect to execute the API request and state collection only once
                LaunchedEffect(cardViewModel) {
                    cardViewModel.resetResultState()
                    cardViewModel.createCardVaultTokenDetails()
                }
                LaunchedEffect(threeDSViewModel) {
                    threeDSViewModel.resetResultState()
                }
                val vaultToken = cardUIState.token
                val threeDSToken = threeDSUIState.token
                when {
                    !threeDSToken.isNullOrBlank() -> {
                        ThreeDSWidget(token = threeDSToken) { result ->
                            result.onSuccess {
                                context.toast("3DS Result returned [$it]")
                            }.onFailure {
                                context.toast("3DS Result failed! [$it]")
                            }
                        }
                    }

                    !vaultToken.isNullOrBlank() -> {
                        cardViewModel.resetResultState()
                        threeDSViewModel.createStandalone3dsToken(vaultToken)
                    }

                    !cardUIState.error.isNullOrBlank() -> {
                        context.toast(cardUIState.error ?: TOKENISE_CARD_ERROR)
                    }

                    !threeDSUIState.error.isNullOrBlank() -> {
                        context.toast(threeDSUIState.error ?: THREE_DS_CARD_ERROR)
                    }

                    cardUIState.isLoading || threeDSUIState.isLoading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}