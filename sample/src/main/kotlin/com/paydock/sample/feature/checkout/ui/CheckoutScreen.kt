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
        // This will expand the modal fully based on the size
        skipPartiallyExpanded = true
    )

    var open3DSBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottom3DSSheetState = rememberModalBottomSheetState(
        // This will expand the modal fully based on the size
        skipPartiallyExpanded = true
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