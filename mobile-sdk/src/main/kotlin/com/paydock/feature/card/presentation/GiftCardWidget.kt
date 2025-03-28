package com.paydock.feature.card.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.components.CardPinInput
import com.paydock.feature.card.presentation.components.GiftCardNumberInput
import com.paydock.feature.card.presentation.state.GiftCardUIState
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A composable for capturing and processing gift card details.
 *
 * This widget provides inputs for entering the card number and PIN, as well as a button to submit
 * the details for tokenization. The component handles state changes and communicates the result
 * via a callback.
 *
 * @param modifier The modifier to be applied to the widget.
 * @param enabled Controls whether the widget is enabled or disabled. When `false`, the widget
 *                will appear disabled and not respond to user interactions.
 * @param accessToken The access token used for authenticating API calls.
 * @param storePin A flag to determine if the PIN should be stored for the initial transaction.
 * @param loadingDelegate An optional delegate to manage the visibility of loading indicators externally.
 * @param completion A callback invoked with the result of the tokenization process, providing either
 *                   a success with the token or a failure with an exception.
 */
@Suppress("MagicNumber")
@Composable
fun GiftCardWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessToken: String,
    storePin: Boolean = true,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<String>) -> Unit,
) {
    // ViewModel instance scoped to the Koin dependency injection framework
    val viewModel: GiftCardViewModel = koinViewModel(parameters = { parametersOf(accessToken) })
    viewModel.setStorePin(storePin)

    // Observing state flows for input and UI state
    val inputState by viewModel.inputStateFlow.collectAsState()
    val uiState by viewModel.stateFlow.collectAsState()

    // Focus handlers for input fields
    val focusCardNumber = FocusRequester()
    val focusCardPin = FocusRequester()

    // React to changes in the UI state
    LaunchedEffect(uiState) {
        handleUIState(uiState, viewModel, loadingDelegate, completion)
    }

    // Composing the UI
    SdkTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Theme.colors.background),
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
                // Card number input field
                GiftCardNumberInput(
                    modifier = Modifier
                        .weight(0.7f)
                        .focusRequester(focusCardNumber)
                        .testTag("cardNumberInput"),
                    value = inputState.cardNumber,
                    enabled = uiState !is GiftCardUIState.Loading && enabled,
                    onValueChange = { viewModel.updateCardNumber(it) },
                    nextFocus = focusCardPin
                )

                // PIN input field
                CardPinInput(
                    modifier = Modifier
                        .weight(0.3f)
                        .focusRequester(focusCardPin)
                        .testTag("cardPinInput"),
                    value = inputState.pin,
                    enabled = uiState !is GiftCardUIState.Loading && enabled,
                    onValueChange = { viewModel.updateCardPin(it) }
                )
            }

            // Submit button
            SdkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("addCard"),
                text = stringResource(R.string.button_submit),
                enabled = inputState.isDataValid && uiState !is GiftCardUIState.Loading && enabled,
                isLoading = loadingDelegate == null && uiState is GiftCardUIState.Loading
            ) {
                viewModel.tokeniseCard()
            }
        }
    }
}

/**
 * Handles changes in the UI state and performs the corresponding actions.
 *
 * - Starts and stops the loading indicator via the loading delegate.
 * - Invokes the completion callback with the result of the operation.
 * - Resets the UI state to idle after handling the current state.
 *
 * @param uiState The current state of the UI.
 * @param viewModel The `GiftCardViewModel` responsible for managing the state.
 * @param loadingDelegate An optional delegate for controlling loading state externally.
 * @param completion The callback to notify of the tokenization result.
 */
private fun handleUIState(
    uiState: GiftCardUIState,
    viewModel: GiftCardViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    completion: (Result<String>) -> Unit,
) {
    when (uiState) {
        is GiftCardUIState.Idle -> Unit
        is GiftCardUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }

        is GiftCardUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(uiState.token))
            viewModel.resetResultState()
        }

        is GiftCardUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            viewModel.resetResultState()
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewGiftCardDetails() {
    SdkTheme {
        GiftCardWidget(accessToken = "accessToken") {

        }
    }
}