package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.card.CardAppearanceDefaults
import com.paydock.designsystems.components.loader.OverlayLoaderAppearanceDefaults
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.integrated.presentation.MPGS3dsWidget
import com.paydock.feature.threeDS.integrated.presentation.ui.MPGSThreeDSWidgetAppearanceDefaults
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWidgetAppearance
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
                        appearance = MPGSThreeDSWidgetAppearanceDefaults.appearance()
                    ) { result ->
                        viewModel.handleMPGS3dsResult(result)
                    }
                }

                ThreeDSType.STANDALONE -> {
                    Standalone3DSWidget(
                        config = ThreeDSConfig(token = threeDSToken),
                        appearance = StandaloneThreeDSWidgetAppearance(
                            loader = OverlayLoaderAppearanceDefaults.appearance().copy(
                                backgroundColor = Color.White,
                                cardAppearance = CardAppearanceDefaults.appearance().copy(
                                    contentPadding = PaddingValues(32.dp)
                                ),
                                loaderSize = 40.dp,
                                loaderSpacing = 32.dp,
                                loaderText = "Processing payment...",
                                loaderTextAppearance = TextAppearanceDefaults.appearance().copy(
                                    style = TextStyle(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                ),
                            )
                        ),
                        // Drive the host's full-screen loader (EnhancedCheckoutScreen shows a
                        // blocking Dialog when viewModel.isLoading) instead of the widget's
                        // built-in overlay, matching the other checkout payment methods.
                        loadingDelegate = object : WidgetLoadingDelegate {
                            override fun widgetLoadingDidStart() {
                                viewModel.setIsLoading(true)
                            }

                            override fun widgetLoadingDidFinish() {
                                viewModel.setIsLoading(false)
                            }
                        }
                    ) { result ->
                        viewModel.handleStandalone3DSResult(result)
                    }
                }
            }
        }
    }
}