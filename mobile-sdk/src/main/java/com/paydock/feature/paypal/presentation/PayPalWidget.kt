/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
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

package com.paydock.feature.paypal.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.PAY_PAL_REDIRECT_URL
import com.paydock.core.data.network.error.exceptions.WebViewException
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.presentation.ui.extensions.alpha40
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.theme.PayPal
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.paypal.presentation.state.PayPalData
import com.paydock.feature.paypal.presentation.viewmodels.PayPalViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable for handling PayPal payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param requestShipping Flag passed to determine if PayPal will ask the user for their shipping address.
 * @param completion A callback to handle the Wallet Charge result.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun PayPalWidget(
    modifier: Modifier = Modifier,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    requestShipping: Boolean = true,
    completion: (Result<ChargeResponse>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: PayPalViewModel = koinViewModel()

    // Collect states for PayPal view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    BackHandler(bottomSheetState.isVisible) {
        scope.launch {
            bottomSheetState.hide()
            completion(
                Result.failure(
                    WebViewException.PayPalException(
                        displayableMessage = context.getString(
                            R.string.error_paypal_cancelled_error
                        )
                    )
                )
            )
            viewModel.resetResultState()
        }
    }

    // Handle token result and reset the payment state
    LaunchedEffect(uiState.paymentData) {
        uiState.paymentData?.let { paymentData ->
            openBottomSheet = false
            val walletToken = uiState.token
            if (!walletToken.isNullOrBlank()) {
                handlePayPalPaymentResult(paymentData, walletToken, viewModel)
            } else {
                completion(
                    Result.failure(
                        WebViewException.PayPalException(
                            displayableMessage = context.getString(
                                R.string.error_web_unknown_error
                            )
                        )
                    )
                )
                viewModel.resetResultState()
            }
        }
    }

    // Handle wallet response result and reset state
    LaunchedEffect(uiState) {
        // Handle wallet error flow and display
        uiState.error?.let {
            // Send error state to the completion callback
            completion(Result.failure(WebViewException.PayPalException(displayableMessage = it.displayableMessage)))
            viewModel.resetResultState()
        }

        // Handle wallet callback response result to show the bottom sheet
        uiState.callbackData?.let {
            openBottomSheet = true
        }

        // Handle wallet charge response result and reset state
        uiState.chargeData?.let { response ->
            completion(Result.success(response))
            viewModel.resetResultState()
        }
    }

    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Button to initiate PayPal transaction
                Button(
                    onClick = {
                        // Use the callback to obtain the token asynchronously
                        token { obtainedToken ->
                            scope.launch {
                                viewModel.setWalletToken(obtainedToken)
                                viewModel.getWalletCallback(
                                    walletToken = obtainedToken,
                                    requestShipping = requestShipping
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("payPalButton")
                        .fillMaxWidth()
                        .height(Theme.dimensions.buttonHeight),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayPal,
                        disabledContainerColor = PayPal.alpha40
                    ),
                    shape = Theme.shapes.small
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_paypal_button),
                        contentDescription = stringResource(id = R.string.content_desc_paypal_button_icon)
                    )
                }
            }

            // Sheet content
            if (openBottomSheet) {
                uiState.callbackData?.callbackUrl?.let {
                    // Create and load the PayPal URL
                    val payPalUrl: String by rememberSaveable {
                        mutableStateOf(
                            it.let { callbackUrl ->
                                viewModel.createPayPalUrl(callbackUrl)
                            }
                        )
                    }
                    SdkBottomSheet(
                        bottomSheetState = bottomSheetState,
                        onDismissRequest = {
                            openBottomSheet = false
                            completion(
                                Result.failure(
                                    WebViewException.PayPalException(
                                        displayableMessage = context.getString(
                                            R.string.error_paypal_cancelled_error
                                        )
                                    )
                                )
                            )
                        }
                    ) {
                        SdkWebView<Unit>(
                            webUrl = payPalUrl,
                            showLoader = false,
                            onShouldOverrideUrlLoading = { request ->
                                val requestUrl = request?.url.toString()
                                // Handle redirection URLs
                                return@SdkWebView if (requestUrl.startsWith(PAY_PAL_REDIRECT_URL)) {
                                    viewModel.parsePayPalUrl(requestUrl)
                                    true
                                } else {
                                    null
                                }
                            }
                        ) { status, message ->
                            // Hide the bottom sheet on WebView error and handle the sheet dismissal
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                if (!bottomSheetState.isVisible) {
                                    openBottomSheet = false
                                }
                            }
                            // Invoke the onWebViewError callback with the PayPal exception
                            completion(Result.failure(WebViewException.PayPalException(code = status, displayableMessage = message)))
                        }
                    }
                }
            }

            // Show a progress indicator if wallet is loading
            if (uiState.isLoading) {
                SdkLoader()
            }
        }
    }
}

/**
 * Handle a successful PayPal payment
 *
 * @param paymentData payment object containing the parsed data from the redirect url.
 * @param token The wallet token provided for initiating the wallet charge
 * @param viewModel [PayPalViewModel] responsible for capturing the wallet charge
 * @see [Payment
 * Data](https://developers.google.com/pay/api/android/reference/request-objects#PaymentData)
 */
internal fun handlePayPalPaymentResult(
    paymentData: PayPalData,
    token: String,
    viewModel: PayPalViewModel
) {
    viewModel.captureWalletTransaction(
        walletToken = token,
        paymentMethodId = paymentData.payPalToken,
        payerId = paymentData.payerId
    )
}

@LightDarkPreview
@Composable
private fun PreviewPayPalWidget() {
    SdkTheme {
        PayPalWidget(token = {}, completion = {})
    }
}