package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.feature.threeDS.presentation.ThreeDSWidget
import com.paydock.sample.feature.checkout.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Checkout3DSBottomSheet(
    bottom3DSSheetState: SheetState,
    onDismissRequest: () -> Unit,
    vaultToken: String?,
    threeDSToken: String?,
    viewModel: CheckoutViewModel
) {
    if (!vaultToken.isNullOrBlank() && !threeDSToken.isNullOrBlank()) {
        SdkBottomSheet(
            containerColor = Color.White,
            bottomSheetState = bottom3DSSheetState,
            onDismissRequest = onDismissRequest
        ) {
            ThreeDSWidget(token = threeDSToken) { result ->
                onDismissRequest()
                viewModel.handleThreeDSResult(
                    vaultToken,
                    result
                )
            }
        }
    }
}