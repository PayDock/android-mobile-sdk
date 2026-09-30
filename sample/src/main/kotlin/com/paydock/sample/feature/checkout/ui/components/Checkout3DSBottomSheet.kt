package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.integrated.presentation.MPGS3dsWidget
import com.paydock.feature.threeDS.integrated.presentation.ui.MPGSThreeDSWidgetAppearanceDefaults
import com.paydock.sample.feature.checkout.models.ThreeDSType
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Checkout3DSBottomSheet(
    bottom3DSSheetState: SheetState,
    onDismissRequest: () -> Unit,
    vaultToken: String?,
    threeDSToken: String?,
    threeDSType: ThreeDSType,
    showCloseButton: Boolean = true,
    viewModel: EnhancedCheckoutViewModel
) {
    SdkBottomSheet(
        containerColor = Color.White,
        bottomSheetState = bottom3DSSheetState,
        shouldDismissOnBackPress = false,
        onDismissRequest = onDismissRequest,
        enableClose = showCloseButton
    ) {
        when (threeDSType) {
            ThreeDSType.MPGS -> {
                if (!vaultToken.isNullOrBlank() && !threeDSToken.isNullOrBlank()) {
                    MPGS3dsWidget(
                        config = ThreeDSConfig(token = threeDSToken),
                        appearance = MPGSThreeDSWidgetAppearanceDefaults.appearance()
                    ) { result ->
                        viewModel.handleMPGS3dsResult(result)
                    }
                }
            }

            ThreeDSType.STANDALONE -> {
                // Phases (preparing → verifying → challenge → finalizing → result) are driven by the
                // widget's onProgress/completion callbacks; see Standalone3DSSheetContent.
                viewModel.standalone3DSFlow?.let { flow ->
                    Standalone3DSSheetContent(
                        flow = flow,
                        threeDSToken = threeDSToken,
                        onProgress = viewModel::handleStandalone3DSProgress,
                        onResult = viewModel::handleStandalone3DSResult,
                        onRetry = viewModel::retryStandalone3DS,
                        onClose = viewModel::closeStandalone3DSFailure
                    )
                }
            }
        }
    }
}
