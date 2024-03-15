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

package com.paydock.sample.feature.checkout.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.sample.feature.checkout.CheckoutViewModel
import com.paydock.sample.feature.checkout.ui.components.CheckoutContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(viewModel: CheckoutViewModel = hiltViewModel()) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    var open3DSBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottom3DSSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    val uiState by viewModel.stateFlow.collectAsState()

    CheckoutContent(
        openBottomSheet = openBottomSheet,
        bottomSheetState = bottomSheetState,
        open3DSBottomSheet = open3DSBottomSheet,
        bottom3DSSheetState = bottom3DSSheetState,
        uiState = uiState,
        onCheckoutButtonClick = {
            viewModel.resetResultState()
            openBottomSheet = true
        },
        onShow3DSBottomSheet = { open3DSBottomSheet = true },
        onDismissBottomSheet = { openBottomSheet = false },
        onDismiss3DSBottomSheet = { open3DSBottomSheet = false },
        viewModel = viewModel
    )
}