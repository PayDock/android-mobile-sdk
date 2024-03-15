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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.core.presentation.ui.extensions.toast
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.dialogs.ErrorDialog
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.feature.checkout.CheckoutUIState
import com.paydock.sample.feature.checkout.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutContent(
    openBottomSheet: Boolean,
    bottomSheetState: SheetState,
    open3DSBottomSheet: Boolean,
    bottom3DSSheetState: SheetState,
    uiState: CheckoutUIState,
    onCheckoutButtonClick: () -> Unit,
    onShow3DSBottomSheet: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onDismiss3DSBottomSheet: () -> Unit,
    viewModel: CheckoutViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(uiState.chargeResult) {
        uiState.chargeResult?.let {
            // TODO - Implement a success screen state
            context.toast("Transaction Successful")
            onDismissBottomSheet()
        }
    }

    LaunchedEffect(uiState.walletChargeResult) {
        uiState.walletChargeResult?.let { charge ->
            if (charge.status == "complete") {
                // TODO - Implement a success screen state
                context.toast("Transaction Successful")
                onDismissBottomSheet()
            }
        }
    }

    LaunchedEffect(uiState.threeDSToken) {
        uiState.threeDSToken?.let {
            onShow3DSBottomSheet()
        }
    }

    if (!uiState.error.isNullOrBlank()) {
        ErrorDialog(
            onDismissRequest = { viewModel.resetResultState() },
            onConfirmation = {
                viewModel.resetResultState()
            },
            dialogText = uiState.error,
        )
    }

    Column {
        Divider(color = Theme.colors.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your cart",
                fontSize = 20.sp,
            )
            BasketItemView(
                title = "ThinkPad X1 Yoga Gen 7",
                description = "ThinkPad X1 Yoga Gen 7\n14” Intel 2 in 1 Laptop",
                price = "£3,299",
                image = R.drawable.demo_product_1
            )
            BasketItemView(
                title = "Galaxy S23 Ultra",
                description = "SM-S918BZGHSEK\n512 GB｜12 GB｜Green",
                price = "£2,199",
                image = R.drawable.demo_product_2
            )
            Spacer(modifier = Modifier.weight(1.0f))
            TotalRowView(title = "Subtotal", value = "£5,498", color = Theme.colors.outlineVariant)
            TotalRowView(title = "Shipping", value = "Free", color = Theme.colors.outlineVariant)
            Divider(color = Theme.colors.outlineVariant)
            TotalRowView(title = "Total", value = "£5,498", color = Theme.colors.onBackground)
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Checkout",
                onClick = onCheckoutButtonClick
            )
        }
    }

    if (openBottomSheet) {
        CheckoutBottomSheet(
            bottomSheetState = bottomSheetState,
            onDismissRequest = { onDismissBottomSheet() },
            uiState = uiState,
            viewModel = viewModel
        )
    }

    if (open3DSBottomSheet) {
        Checkout3DSBottomSheet(
            bottom3DSSheetState = bottom3DSSheetState,
            onDismissRequest = { onDismiss3DSBottomSheet() },
            vaultToken = uiState.vaultToken,
            threeDSToken = uiState.threeDSToken?.token,
            viewModel = viewModel
        )
    }
}