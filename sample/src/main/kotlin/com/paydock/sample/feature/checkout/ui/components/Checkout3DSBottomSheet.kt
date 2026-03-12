package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSAppearanceDefaults
import com.paydock.feature.threeDS.integrated.presentation.MPGS3dsWidget
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget
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
    if (!vaultToken.isNullOrBlank() && !threeDSToken.isNullOrBlank()) {
        SdkBottomSheet(
            containerColor = Color.White,
            bottomSheetState = bottom3DSSheetState,
            shouldDismissOnBackPress = false,
            onDismissRequest = onDismissRequest,
            enableClose = showCloseButton
        ) {
            when (threeDSType) {
                ThreeDSType.MPGS -> {
                    MPGS3dsWidget(
                        config = ThreeDSConfig(token = threeDSToken),
                        appearance = ThreeDSAppearanceDefaults.appearance()
                    ) { result ->
                        viewModel.handleMPGS3dsResult(result)
                    }
                }

                ThreeDSType.STANDALONE -> {
                    Standalone3DSWidget(
                        config = ThreeDSConfig(token = threeDSToken),
                        appearance = ThreeDSAppearanceDefaults.appearance()
                    ) { result ->
                        viewModel.handleStandalone3DSResult(result)
                    }
                }
            }
        }
    }
}