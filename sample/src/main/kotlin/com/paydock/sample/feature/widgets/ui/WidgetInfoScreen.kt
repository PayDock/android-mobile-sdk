package com.paydock.sample.feature.widgets.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.toError
import com.paydock.core.domain.model.meta.ClickToPayMeta
import com.paydock.core.network.dto.error.displayableMessage
import com.paydock.core.presentation.ui.extensions.toast
import com.paydock.feature.address.presentation.AddressDetailsWidget
import com.paydock.feature.afterpay.presentation.AfterpayWidget
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOptionUpdate
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.feature.card.presentation.GiftCardWidget
import com.paydock.feature.card.presentation.model.SaveCardConfig
import com.paydock.feature.flypay.presentation.FlyPayWidget
import com.paydock.feature.googlepay.presentation.GooglePayWidget
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.paypal.presentation.PayPalWidget
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.feature.threeDS.presentation.ThreeDSWidget
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.feature.card.CardViewModel
import com.paydock.sample.feature.settings.SettingsViewModel
import com.paydock.sample.feature.threeDS.presentation.ThreeDSViewModel
import com.paydock.sample.feature.wallet.presentation.WalletViewModel
import com.paydock.sample.feature.widgets.ui.models.WidgetType
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Currency

@Composable
fun WidgetInfoScreen(
    widgetType: WidgetType,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel(),
    cardViewModel: CardViewModel = hiltViewModel(),
    threeDSViewModel: ThreeDSViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val accessToken by settingsViewModel.accessToken.collectAsState()
        val context = LocalContext.current
        when (widgetType) {
            WidgetType.CREDIT_CARD_DETAILS -> {
                CardDetailsWidget(
                    modifier = Modifier.padding(16.dp),
                    accessToken = accessToken,
                    gatewayId = BuildConfig.GATEWAY_ID,
                    allowSaveCard = SaveCardConfig(
                        privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                            privacyPolicyURL = "https://www.google.com"
                        )
                    ),
                    completion = { result ->
                        // This breaks down 3 ways to retrieve and handle the result
                        // Option 1: Default Result Handler
                        result.onSuccess {
                            Log.d("[CardDetailsWidget]", "Success: $it")
                            context.toast("Tokenised card was successful! [$it]")
                        }.onFailure { exception: Throwable ->
                            if (exception is CardDetailsException) {
                                val error = when (exception) {
                                    is CardDetailsException.TokenisingCardException -> exception.error.displayableMessage
                                    is CardDetailsException.UnknownException -> exception.message
                                }
                                Log.d("[CardDetailsWidget]", "Failure: $error")
                                context.run { toast("Tokenised card failed! [${error}]") }
                            }
                        }

                        // Option 2: Use try catch block with getOrThrow extension function
//                        try {
//                            val token: String = result.getOrThrow(CardDetailsException::class)
//                            Log.d("[CardDetailsWidget]", "Success: $token")
//                            context.toast("Tokenised card was successful! [$token]")
//                        } catch (exception: CardDetailsException) {
//                            when (exception) {
//                                is CardDetailsException.TokenisingCardException -> TODO()
//                                is CardDetailsException.UnknownException -> TODO()
//                            }
//                        } catch (e: Exception) {
//                            // Catch generic exceptions
//                            val error = e.toError()
//                            Log.d("[WidgetFailure]", "Failure: ${error.displayableMessage}")
//                        }

                        // Option 3: Use onFailure extension function to specify Exception
//                        result.onSuccess {
//                            Log.d("[CardDetailsWidget]", "Success: $it")
//                            context.toast("Tokenised card was successful! [$it]")
//                        }.onFailure(CardDetailsException::class) { exception: CardDetailsException ->
//                                when (exception) {
//                                    is CardDetailsException.TokenisingCardException -> TODO()
//                                    is CardDetailsException.UnknownException -> TODO()
//                                }
//                            }.recoverCatching {
//                                // Catch generic exceptions
//                                val error = it.toError()
//                                Log.d("[CardDetailsWidget]", "Failure: ${error.displayableMessage}")
//                            }
                    })
            }

            WidgetType.GIFT_CARD_DETAILS -> {
                GiftCardWidget(
                    accessToken = accessToken,
                    storePin = true, completion = { result ->
                        result.onSuccess {
                            Log.d("[GiftCardWidget]", "Success: $it")
                            context.toast("Tokenised card was successful! [$it]")
                        }.onFailure {
                            val error = it.toError()
                            Log.d("[GiftCardWidget]", "Failure: ${error.displayableMessage}")
                            context.toast("Tokenised card failed! [${error.displayableMessage}]")
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
                        context.toast("Google Pay Result returned [$it]")
                    }.onFailure {
                        val error = it.toError()
                        Log.d("[GooglePayWidget]", "Failure: ${error.displayableMessage}")
                        context.toast("Google Pay Result failed! [${error.displayableMessage}]")
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

            WidgetType.CLICK_TO_PAY -> {
                // This is to ensure we hide the WebView once completed
                var hasCompletedFlow: Boolean by remember { mutableStateOf(false) }
                if (!hasCompletedFlow) {
                    // Test Cards: https://developer.mastercard.com/unified-checkout-solutions/documentation/testing/test_cases/click_to_pay_case/#test-cards
                    ClickToPayWidget(
                        modifier = Modifier
                            .fillMaxWidth(),
                        accessToken = accessToken,
                        serviceId = BuildConfig.GATEWAY_ID_MASTERCARD_SRC,
                        meta = ClickToPayMeta(
                            disableSummaryScreen = true
                        )
                    ) { result ->
                        result.onSuccess {
                            Log.d("[ClickToPayWidget]", it)
                            context.toast("ClickToPay Result returned [$it]")
                            hasCompletedFlow = true
                        }.onFailure {
                            val error = it.toError()
                            Log.d("[ClickToPayWidget]", error.displayableMessage)
                            context.toast("ClickToPay Result failed! [${error.displayableMessage}]")
                            hasCompletedFlow = true
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
                    token = walletViewModel.getWalletToken(WalletType.PAY_PAL)
                ) { result ->
                    result.onSuccess {
                        Log.d("[PayPalWidget]", "Success: $it")
                        context.toast("PayPal Result returned [$it]")
                    }.onFailure {
                        val error = it.toError()
                        Log.d("[PayPalWidget]", "Failure: ${error.displayableMessage}")
                        context.toast("PayPal Result failed! [${error.displayableMessage}]")
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
                    token = walletViewModel.getWalletToken(WalletType.FLY_PAY)
                ) { result ->
                    result.onSuccess {
                        Log.d("[FlyPayWidget]", "Success: $it")
                        context.toast("FlyPay Result returned [$it]")
                    }.onFailure {
                        val error = it.toError()
                        Log.d("[FlyPayWidget]", "Failure: ${error.displayableMessage}")
                        context.toast("FlyPay Result failed! [${error.displayableMessage}]")
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

            WidgetType.AFTER_PAY -> {
                val uiState by walletViewModel.stateFlow.collectAsState()

                val configuration = AfterpaySDKConfig(
                    config = AfterpaySDKConfig.AfterpayConfiguration(
                        maximumAmount = "100",
                        currency = AU_CURRENCY_CODE,
                        language = "en",
                        country = AU_COUNTRY_CODE
                    ),
                    options = AfterpaySDKConfig.CheckoutOptions(
                        shippingOptionRequired = true,
                        enableSingleShippingOptionUpdate = true
                    )
                )
                AfterpayWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    token = walletViewModel.getWalletToken(WalletType.AFTER_PAY),
                    config = configuration,
                    selectAddress = { _, provideShippingOptions ->
                        val currency = Currency.getInstance(configuration.config.currency)
                        val shippingOptions = listOf(
                            AfterpayShippingOption(
                                "standard",
                                "Standard",
                                "",
                                currency,
                                "0.00".toBigDecimal(),
                                "50.00".toBigDecimal(),
                                "0.00".toBigDecimal(),
                            ),
                            AfterpayShippingOption(
                                "priority",
                                "Priority",
                                "Next business day",
                                currency,
                                "10.00".toBigDecimal(),
                                "60.00".toBigDecimal(),
                                null,
                            )
                        )
                        provideShippingOptions(shippingOptions)
                    },
                    selectShippingOption = { shippingOption, provideShippingOptionUpdateResult ->
                        val currency = Currency.getInstance(configuration.config.currency)
                        // if standard shipping was selected, update the amounts
                        // otherwise leave as is by passing null
                        val result: AfterpayShippingOptionUpdate? =
                            if (shippingOption.id == "standard") {
                                AfterpayShippingOptionUpdate(
                                    "standard",
                                    currency,
                                    "0.00".toBigDecimal(),
                                    "50.00".toBigDecimal(),
                                    "2.00".toBigDecimal(),
                                )
                            } else {
                                null
                            }
                        provideShippingOptionUpdateResult(result)
                    }
                ) { result ->
                    result.onSuccess {
                        Log.d("[AfterpayWidget]", "Success: $it")
                        context.toast("Afterpay Result returned [$it]")
                    }.onFailure {
                        val error = it.toError()
                        Log.d("[AfterpayWidget]", "Failure: ${error.displayableMessage}")
                        context.toast("Afterpay Result failed! [${error.displayableMessage}]")
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
                                Log.d("[Integrated-3DS]", "Success: $it")
                                context.toast("3DS Result returned [$it]")
                            }.onFailure {
                                val error = it.toError()
                                Log.d("[Integrated-3DS]", "Failure: ${error.displayableMessage}")
                                context.toast("3DS Result failed! [${error.displayableMessage}]")
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
                                Log.d("[Standalone-3DS]", "Success: $it")
                                context.toast("3DS Result returned [$it]")
                            }.onFailure {
                                val error = it.toError()
                                Log.d("[Standalone-3DS]", "Failure: ${error.displayableMessage}")
                                context.toast("3DS Result failed! [${error.displayableMessage}]")
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