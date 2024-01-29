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

package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.feature.checkout.CheckoutUIState
import com.paydock.sample.feature.checkout.CheckoutViewModel
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    uiState: CheckoutUIState,
    viewModel: CheckoutViewModel
) {
    val scrollState = rememberScrollState()
    val supportedPaymentMethods =
        listOf(
            WidgetType.CREDIT_CARD_DETAILS,
            WidgetType.GOOGLE_PAY,
            WidgetType.PAY_PAL,
            WidgetType.FLY_PAY
        )
    var selectedTab by remember { mutableStateOf(supportedPaymentMethods.first()) }
    SdkBottomSheet(
        containerColor = Color.White,
        bottomSheetState = bottomSheetState,
        onDismissRequest = onDismissRequest
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // If we want to only show the loader in the sheet, we would need to cater for either maintaining selected state to prevent it resetting
            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethodContainer(
                    paymentMethods = supportedPaymentMethods,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                    })

                // Component based on the selected tab with animation
                Crossfade(
                    targetState = selectedTab,
                    label = "content cross fade"
                ) { targetTab ->
                    when (targetTab) {
                        WidgetType.CREDIT_CARD_DETAILS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardContent(viewModel)
                                AppButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Pay AUD 10.00",
                                    enabled = !uiState.cardToken.isNullOrBlank() && !uiState.isLoading
                                ) {
                                    val token = uiState.cardToken
                                    token?.let { viewModel.createSessionVaultToken(cardToken = it) }
                                }
                            }
                        }

                        WidgetType.GOOGLE_PAY -> GooglePayContent(viewModel)
                        WidgetType.PAY_PAL -> PayPalContent(viewModel)
                        WidgetType.FLY_PAY -> FlyPayContent(viewModel)
                        else -> Unit
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}