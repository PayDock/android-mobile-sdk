package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.components.CardPinInput
import com.paydock.feature.card.presentation.components.GiftCardNumberInput
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A composable for capturing and displaying card details.
 *
 * @param modifier The modifier to apply to this composable.
 * @param storePin A flag to be able to use a PIN value for the initial transaction.
 * @param completion A callback to receive the result of card details processing.
 */
@Suppress("LongMethod", "MagicNumber")
@Composable
fun GiftCardWidget(
    modifier: Modifier = Modifier,
    storePin: Boolean = true,
    completion: (Result<String>) -> Unit
) {
    val viewModel: GiftCardViewModel = koinViewModel()
    viewModel.setStorePin(storePin)
    val uiState = viewModel.stateFlow.collectAsState()

    val focusCardNumber = FocusRequester()
    val focusCardPin = FocusRequester()

    // Handle error display
    LaunchedEffect(uiState.value.error) {
        uiState.value.error?.let {
            completion(Result.failure(it))
        }
    }

    // Handle token result and reset state
    LaunchedEffect(uiState.value.token) {
        uiState.value.token?.let { token ->
            completion(Result.success(token))
            viewModel.resetResultState()
        }
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // UI composition starts here
    SdkTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(Theme.dimensions.spacing),
            verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    Theme.dimensions.spacing,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Top
            ) {
                // Card number input
                GiftCardNumberInput(
                    modifier = Modifier
                        .weight(0.7f)
                        .focusRequester(focusCardNumber)
                        .testTag("cardNumberInput"),
                    value = uiState.value.cardNumber,
                    enabled = !uiState.value.isLoading,
                    onValueChange = { viewModel.updateCardNumber(it) },
                    nextFocus = focusCardPin
                )

                CardPinInput(
                    modifier = Modifier
                        .weight(0.3f)
                        .focusRequester(focusCardPin)
                        .testTag("cardPinInput"),
                    value = uiState.value.pin,
                    enabled = !uiState.value.isLoading,
                    onValueChange = { viewModel.updateCardPin(it) }
                )
            }

            // Add Card button
            SdkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("addCard"),
                text = stringResource(R.string.button_submit),
                enabled = uiState.value.isDataValid && !uiState.value.isLoading,
                isLoading = uiState.value.isLoading
            ) {
                viewModel.tokeniseCard()
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewGiftCardDetails() {
    SdkTheme {
        GiftCardWidget {

        }
    }
}