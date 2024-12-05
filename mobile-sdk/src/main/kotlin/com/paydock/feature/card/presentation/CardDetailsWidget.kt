package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.components.CardExpiryInput
import com.paydock.feature.card.presentation.components.CardHolderNameInput
import com.paydock.feature.card.presentation.components.CardSecurityCodeInput
import com.paydock.feature.card.presentation.components.CreditCardNumberInput
import com.paydock.feature.card.presentation.components.SaveCardToggle
import com.paydock.feature.card.presentation.state.CardDetailsInputState
import com.paydock.feature.card.presentation.state.CardDetailsUIState
import com.paydock.feature.card.presentation.utils.CardIssuerValidator
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Composable function that renders the Card Details Widget UI.
 *
 * This widget handles the input of card details, including cardholder name,
 * card number, expiry date, and security code. It validates and tokenizes
 * the input when the user submits the form. The state is managed using a
 * `CardDetailsViewModel`.
 *
 * @param modifier Modifier for styling and layout customization.
 * @param enabled Controls the enabled state of this Widget. When false,
 * this component will not respond to user input, and it will appear visually disabled.
 * @param accessToken The access token required for API requests.
 * @param gatewayId Optional ID of the payment gateway for card processing.
 * @param collectCardholderName Whether to show and collect the cardholder's name.
 * @param actionText Text displayed on the submit button (default: "Submit").
 * @param showCardTitle Whether to show a title above the card inputs (default: true).
 * @param allowSaveCard Configuration for saving card details (nullable).
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param completion Callback invoked with the result of the tokenization (success or failure).
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun CardDetailsWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessToken: String,
    gatewayId: String? = null,
    collectCardholderName: Boolean = true,
    actionText: String = stringResource(R.string.button_submit),
    showCardTitle: Boolean = true,
    allowSaveCard: SaveCardConfig? = null,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<CardResult>) -> Unit
) {
    val viewModel: CardDetailsViewModel = koinViewModel(parameters = {
        parametersOf(accessToken, gatewayId)
    })
    viewModel.setCollectCardholderName(collectCardholderName)
    val inputState by viewModel.inputStateFlow.collectAsState()
    val uiState by viewModel.stateFlow.collectAsState()
    val isDataValid by remember(uiState) { derivedStateOf { inputState.isDataValid } }

    val focusCardNumber = FocusRequester()
    val focusExpiration = FocusRequester()
    val focusCVV = FocusRequester()

    // Handles UI state changes (success or failure of tokenization)
    LaunchedEffect(uiState) {
        handleUIState(uiState, inputState, viewModel, loadingDelegate, completion)
    }

    // UI Layout starts here
    SdkTheme {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            // Title for the card information section
            if (showCardTitle) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = Theme.typography.body1,
                    text = stringResource(id = R.string.label_card_information),
                    color = Theme.colors.onSurfaceVariant
                )
            }

            // Input field for cardholder name
            if (collectCardholderName) {
                CardHolderNameInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cardHolderInput"),
                    value = inputState.cardholderName ?: "",
                    enabled = uiState !is CardDetailsUIState.Loading && enabled,
                    nextFocus = focusCardNumber,
                    onValueChange = { viewModel.updateCardholderName(it) }
                )
            }

            // Input field for card number
            CreditCardNumberInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusCardNumber)
                    .testTag("cardNumberInput"),
                value = inputState.cardNumber,
                enabled = uiState !is CardDetailsUIState.Loading && enabled,
                onValueChange = { viewModel.updateCardNumber(it) },
                nextFocus = focusExpiration
            )

            // Row for expiry and security code input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    Theme.dimensions.spacing,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Top
            ) {
                CardExpiryInput(
                    modifier = Modifier
                        .weight(0.5f)
                        .focusRequester(focusExpiration)
                        .testTag("cardExpiryInput"),
                    value = inputState.expiry,
                    enabled = uiState !is CardDetailsUIState.Loading && enabled,
                    onValueChange = { viewModel.updateExpiry(it) },
                    nextFocus = focusCVV
                )

                CardSecurityCodeInput(
                    modifier = Modifier
                        .weight(0.5f)
                        .focusRequester(focusCVV)
                        .testTag("cardSecurityCodeInput"),
                    value = inputState.code,
                    enabled = uiState !is CardDetailsUIState.Loading && enabled,
                    cardIssuer = CardIssuerValidator.detectCardIssuer(inputState.cardNumber),
                    onValueChange = { viewModel.updateSecurityCode(it) }
                )
            }

            // Save card toggle switch (if configured)
            if (allowSaveCard != null) {
                SaveCardToggle(
                    enabled = uiState !is CardDetailsUIState.Loading && enabled,
                    saveCard = inputState.saveCard,
                    config = allowSaveCard,
                    onToggle = viewModel::updateSaveCard
                )
            }

            // Submit button for tokenizing the card
            SdkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveCard"),
                text = actionText,
                enabled = isDataValid && uiState !is CardDetailsUIState.Loading && enabled,
                isLoading = loadingDelegate == null && uiState is CardDetailsUIState.Loading
            ) {
                viewModel.tokeniseCard()
            }
        }
    }
}

/**
 * Handles changes in the UI state during the card details process.
 *
 * This function processes various states of the card details flow, such as idle, loading, success,
 * and error. It updates the loading delegate to reflect the loading state, handles success and
 * error results, and resets the ViewModel state when appropriate.
 *
 * @param uiState The current state of the card details process, represented by `CardDetailsUIState`.
 *                Possible states include `Idle`, `Loading`, `Success`, and `Error`.
 * @param inputState The current input state of the card details form, represented by `CardDetailsInputState`.
 *                   This is used to determine additional user inputs, such as whether the card should be saved.
 * @param viewModel The ViewModel responsible for managing the state and logic of the card details process.
 *                  This function calls `resetResultState()` on the ViewModel to clear states when necessary.
 * @param loadingDelegate An optional delegate to handle UI loading indicators.
 *                        It starts and stops loading animations based on the `Loading` state.
 * @param completion A callback function invoked with the result of the card details process.
 *                   - On success: Passes a `CardResult` containing the card token and save card preference.
 *                   - On error: Passes a failure result with the exception encountered.
 */
private fun handleUIState(
    uiState: CardDetailsUIState,
    inputState: CardDetailsInputState,
    viewModel: CardDetailsViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    completion: (Result<CardResult>) -> Unit,
) {
    when (uiState) {
        is CardDetailsUIState.Idle -> Unit // No action needed for idle state.
        is CardDetailsUIState.Loading -> {
            // Start loading animation when in a loading state.
            loadingDelegate?.widgetLoadingDidStart()
        }
        is CardDetailsUIState.Success -> {
            // Stop loading animation and invoke completion with success result.
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(CardResult(token = uiState.token, saveCard = inputState.saveCard)))
            viewModel.resetResultState() // Reset ViewModel state to avoid reuse of the current state.
        }
        is CardDetailsUIState.Error -> {
            // Stop loading animation and invoke completion with failure result.
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            viewModel.resetResultState() // Reset ViewModel state to avoid reuse of the current state.
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewCardDetails() {
    SdkTheme {
        CardDetailsWidget(accessToken = "accessToken") {

        }
    }
}