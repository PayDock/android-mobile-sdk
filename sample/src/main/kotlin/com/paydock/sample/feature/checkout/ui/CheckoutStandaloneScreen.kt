package com.paydock.sample.feature.checkout.ui

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
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
import com.paydock.sample.feature.style.StylingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutStandaloneScreen(
    viewModel: StandaloneCheckoutViewModel = hiltViewModel(),
    stylingViewModel: StylingViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.stateFlow.collectAsState()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    val bottomSheetState =
        rememberModalBottomSheetState(
            // This will expand the modal fully based on the size
            skipPartiallyExpanded = true,
            // Prevents dismissing sheet from hiding on outside press and when dragging
            confirmValueChange = { newState ->
                newState != SheetValue.Hidden //  Stop bottom sheet from hiding on outside press
            }
        )

    var open3DSBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottom3DSSheetState = rememberModalBottomSheetState(
        // This will expand the modal fully based on the size
        skipPartiallyExpanded = true,
        // Prevents dismissing sheet when dragging
        confirmValueChange = { newState ->
            newState != SheetValue.Hidden //  Stop bottom sheet from hiding on outside press
        }
    )

    // Listen for one-time toast events
    LaunchedEffect(Unit) { // Or a key that changes when the screen is entered
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            openBottomSheet = false // Close bottom sheet after toast
        }
    }

    LaunchedEffect(uiState.threeDSToken) {
        open3DSBottomSheet = uiState.threeDSToken != null
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
            viewModel = viewModel,
            stylingViewModel = stylingViewModel
        )
    }

    if (open3DSBottomSheet) {
        Checkout3DSBottomSheet(
            bottom3DSSheetState = bottom3DSSheetState,
            onDismissRequest = {
                open3DSBottomSheet = false
                // Technically this should not happen as it would break the charge flow
                openBottomSheet = false
            },
            vaultToken = uiState.vaultToken,
            threeDSToken = uiState.threeDSToken?.token,
            showCloseButton = !uiState.isLoading,
            viewModel = viewModel,
            stylingViewModel = stylingViewModel
        )
    }
}