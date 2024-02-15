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

package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.data.network.error.exceptions.ComponentException
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.components.CardExpiryInput
import com.paydock.feature.card.presentation.components.CardHolderNameInput
import com.paydock.feature.card.presentation.components.CardSecurityCodeInput
import com.paydock.feature.card.presentation.components.CreditCardNumberInput
import com.paydock.feature.card.presentation.utils.CardIssuerValidator
import com.paydock.feature.card.presentation.viewmodels.CreditCardDetailsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A composable for capturing and displaying card details.
 *
 * @param modifier The modifier to apply to this composable.
 * @param gatewayId The ID of the payment gateway (optional).
 * @param completion A callback to receive the result of card details processing.
 */
@Suppress("LongMethod")
@Composable
fun CardDetailsWidget(
    modifier: Modifier = Modifier,
    gatewayId: String? = null,
    completion: (Result<String>) -> Unit
) {
    val viewModel: CreditCardDetailsViewModel = koinViewModel()
    gatewayId?.let { viewModel.setGatewayId(it) }
    val uiState = viewModel.stateFlow.collectAsState()

    val focusCardNumber = FocusRequester()
    val focusExpiration = FocusRequester()
    val focusCVV = FocusRequester()

    // Handle error display
    LaunchedEffect(uiState.value.error) {
        if (uiState.value.error != null) {
            completion(Result.failure(ComponentException(uiState.value.error.displayableMessage)))
        }
    }

    // Handle token result and reset state
    LaunchedEffect(uiState.value.token) {
        uiState.value.token?.let { token ->
            completion(Result.success(token))
        }
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // UI composition starts here
    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                // Card information label
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = Theme.typography.body1,
                    text = stringResource(id = R.string.label_card_information),
                    color = Theme.colors.onSurfaceVariant
                )

                // Cardholder name input
                CardHolderNameInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cardHolderInput"),
                    value = uiState.value.cardholderName,
                    enabled = !uiState.value.isLoading,
                    nextFocus = focusCardNumber,
                    onValueChange = { viewModel.updateCardholderName(it) }
                )

                // Card number input
                CreditCardNumberInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusCardNumber)
                        .testTag("cardNumberInput"),
                    value = uiState.value.cardNumber,
                    enabled = !uiState.value.isLoading,
                    onValueChange = { viewModel.updateCardNumber(it) },
                    nextFocus = focusExpiration
                )

                // Expiry and CVV input fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        Theme.dimensions.spacing,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.Top
                ) {
                    CardExpiryInput(
                        modifier = Modifier
                            .weight(0.5f)
                            .focusRequester(focusExpiration)
                            .testTag("cardExpiryInput"),
                        value = uiState.value.expiry,
                        enabled = !uiState.value.isLoading,
                        onValueChange = { viewModel.updateExpiry(it) },
                        nextFocus = focusCVV
                    )

                    CardSecurityCodeInput(
                        modifier = Modifier
                            .weight(0.5f)
                            .focusRequester(focusCVV)
                            .testTag("cardSecurityCodeInput"),
                        value = uiState.value.code,
                        enabled = !uiState.value.isLoading,
                        cardIssuer = CardIssuerValidator.detectCardIssuer(uiState.value.cardNumber),
                        onValueChange = { viewModel.updateSecurityCode(it) }
                    )
                }

                // Save Card button
                SdkButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saveCard"),
                    text = stringResource(R.string.button_submit),
                    enabled = uiState.value.isDataValid && !uiState.value.isLoading,
                    isLoading = uiState.value.isLoading
                ) {
                    viewModel.tokeniseCard()
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewCardDetails() {
    SdkTheme {
        CardDetailsWidget(gatewayId = "") {

        }
    }
}