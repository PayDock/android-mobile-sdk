package com.paydock.sample.feature.checkout.ui

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.sample.designsystems.components.dialogs.ErrorDialog
import com.paydock.sample.feature.checkout.StandaloneCheckoutViewModel
import com.paydock.sample.feature.checkout.ui.components.Checkout3DSBottomSheet
import com.paydock.sample.feature.checkout.ui.components.CheckoutBaseScreen
import com.paydock.sample.feature.checkout.ui.components.CheckoutBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutStandalone(viewModel: StandaloneCheckoutViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.stateFlow.collectAsState()
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

    LaunchedEffect(uiState.chargeResult) {
        uiState.chargeResult?.let {
            // TODO - Implement a success screen state
            Toast.makeText(context, "Transaction Successful", Toast.LENGTH_SHORT).show()
            openBottomSheet = false
        }
    }

    LaunchedEffect(uiState.walletChargeResult) {
        uiState.walletChargeResult?.let { charge ->
            if (charge.status == "complete") {
                // TODO - Implement a success screen state
                Toast.makeText(context, "Transaction Successful", Toast.LENGTH_SHORT).show()
                openBottomSheet = false
            }
        }
    }

    LaunchedEffect(uiState.threeDSToken) {
        uiState.threeDSToken?.let {
            open3DSBottomSheet = true
        }
    }

    if (!uiState.error.isNullOrBlank()) {
        ErrorDialog(
            onDismissRequest = { viewModel.resetResultState() },
            onConfirmation = {
                openBottomSheet = false
                viewModel.resetResultState()
            },
            dialogText = uiState.error!!,
        )
    }

    CheckoutBaseScreen {
        viewModel.resetResultState()
        openBottomSheet = true
    }

    if (openBottomSheet) {
        CheckoutBottomSheet(
            bottomSheetState = bottomSheetState,
            onDismissRequest = { openBottomSheet = false },
            uiState = uiState,
            viewModel = viewModel
        )
    }

    if (open3DSBottomSheet) {
        Checkout3DSBottomSheet(
            bottom3DSSheetState = bottom3DSSheetState,
            onDismissRequest = { open3DSBottomSheet = false },
            vaultToken = uiState.vaultToken,
            threeDSToken = uiState.threeDSToken?.token,
            viewModel = viewModel
        )
    }
}