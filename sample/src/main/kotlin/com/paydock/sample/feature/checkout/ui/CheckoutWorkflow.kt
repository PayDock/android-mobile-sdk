package com.paydock.sample.feature.checkout.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.core.presentation.ui.extensions.toast
import com.paydock.sample.designsystems.components.dialogs.ErrorDialog
import com.paydock.sample.feature.checkout.CheckoutWorkflowViewModel
import com.paydock.sample.feature.checkout.ui.components.CheckoutBaseScreen

@Composable
fun CheckoutWorkflow(viewModel: CheckoutWorkflowViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.stateFlow.collectAsState()

    // Handle intent token result
    LaunchedEffect(uiState.intentToken) {
        uiState.intentToken?.let { token ->
            context.toast("Initiating Payment Workflow with token: $token")
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {

        // Display error dialog if needed
        if (uiState.error != null) {
            ErrorDialog(
                onDismissRequest = viewModel::resetResultState, // Use method reference for conciseness
                onConfirmation = viewModel::resetResultState,
                dialogText = uiState.error!!,
            )
        }

        // Show loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        //Main content
        CheckoutBaseScreen {
            // Use Template Intent Flow
            viewModel.createTemplateIntentToken()
            // Use Direct Intent Flow
//            viewModel.createDirectIntentToken()
        }
    }
}