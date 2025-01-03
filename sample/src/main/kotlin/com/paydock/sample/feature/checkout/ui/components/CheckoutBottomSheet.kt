package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.feature.checkout.CheckoutUIState
import com.paydock.sample.feature.checkout.StandaloneCheckoutViewModel
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    uiState: CheckoutUIState,
    viewModel: StandaloneCheckoutViewModel,
) {
    val scrollState = rememberScrollState()
    val supportedPaymentMethods =
        listOf(
            WidgetType.CARD_DETAILS,
            WidgetType.CLICK_TO_PAY,
            WidgetType.GOOGLE_PAY,
            WidgetType.PAY_PAL,
            WidgetType.FLY_PAY,
            WidgetType.AFTER_PAY
        )
    val accessToken by viewModel.accessToken.collectAsState()
    var selectedTab by remember { mutableStateOf(supportedPaymentMethods.first()) }
    SdkBottomSheet(
        containerColor = Theme.colors.surface,
        bottomSheetState = bottomSheetState,
        onDismissRequest = onDismissRequest
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // If we want to only show the loader in the sheet, we would need to cater for either maintaining selected state to prevent it resetting
            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethodContainer(
                    paymentMethods = supportedPaymentMethods,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                    })

                // Component based on the selected tab with animation
                Crossfade(
                    targetState = selectedTab,
                    label = "content cross fade"
                ) { targetTab ->
                    when (targetTab) {
                        WidgetType.CARD_DETAILS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardContent(
                                    enabled = !uiState.isLoading,
                                    accessToken = accessToken,
                                    loadingDelegate = viewModel,
                                    resultHandler = viewModel::handleCardResult
                                )
                            }
                        }

                        WidgetType.CLICK_TO_PAY -> ClickToPayComponent(
                            isLoading = uiState.isLoading,
                            accessToken = accessToken,
                            resultHandler = viewModel::handleClickToPayResult
                        )

                        WidgetType.GOOGLE_PAY -> GooglePayContent(
                            tokenHandler = viewModel.getWalletToken(
                                WalletType.GOOGLE
                            ),
                            resultHandler = viewModel::handleChargeResult
                        )

                        WidgetType.PAY_PAL -> PayPalContent(
                            enabled = !uiState.isLoading,
                            tokenHandler = viewModel.getWalletToken(WalletType.PAY_PAL),
                            loadingDelegate = viewModel,
                            resultHandler = viewModel::handleChargeResult
                        )

                        WidgetType.FLY_PAY -> FlyPayContent(
                            tokenHandler = viewModel.getWalletToken(WalletType.FLY_PAY),
                            resultHandler = viewModel::handleFlyPayResult
                        )

                        WidgetType.AFTER_PAY -> AfterpayContent(
                            tokenHandler = viewModel.getWalletToken(WalletType.AFTER_PAY),
                            resultHandler = viewModel::handleChargeResult
                        )

                        else -> Unit
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}