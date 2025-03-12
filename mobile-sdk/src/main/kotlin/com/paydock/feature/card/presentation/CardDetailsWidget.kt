package com.paydock.feature.card.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.presentation.components.CardInputFields
import com.paydock.feature.card.presentation.components.SaveCardToggle
import com.paydock.feature.card.presentation.components.SupportedCardBanner
import com.paydock.feature.card.presentation.state.CardDetailsInputState
import com.paydock.feature.card.presentation.state.CardDetailsUIState
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A composable function that renders the Card Details Widget UI.
 *
 * This widget provides an interface for users to input and validate their card details,
 * including cardholder name, card number, expiry date, and security code.
 * It manages the input state, validates the data, and tokenizes it when the form is submitted.
 * The state is managed through a `CardDetailsViewModel`.
 *
 * @param modifier A [Modifier] for styling and layout customization. Use this to adjust spacing, size, or positioning of the widget.
 * @param enabled Determines whether the widget is enabled. If `false`, the widget will appear
 * visually disabled and will not respond to user input.
 * @param config Configuration options for the widget, encapsulated in [CardDetailsWidgetConfig],
 * such as access token, gateway ID, and display options.
 * @param loadingDelegate An optional [WidgetLoadingDelegate] for overriding the default loader
 * behavior during tokenization or other async operations.
 * @param completion A callback invoked with the result of the tokenization process.
 * It provides a [Result] containing a [CardResult] on success or an error on failure.
 */
@Suppress("LongMethod")
@Composable
fun CardDetailsWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: CardDetailsWidgetConfig,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<CardResult>) -> Unit
) {
    val viewModel: CardDetailsViewModel = koinViewModel(parameters = {
        parametersOf(config.accessToken, config.gatewayId, config.schemeSupport)
    })
    viewModel.setCollectCardholderName(config.collectCardholderName)
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
            modifier = modifier
                .fillMaxWidth()
                .background(Theme.colors.background),
            verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            // Title for the card information section
            if (config.showCardTitle) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = Theme.typography.body1,
                    text = stringResource(id = R.string.label_card_information),
                    color = Theme.colors.onSurface
                )
            }
            if (!config.schemeSupport.supportedSchemes.isNullOrEmpty()) {
                SupportedCardBanner(config.schemeSupport.supportedSchemes)
            }
            CardInputFields(
                shouldCollectCardholderName = config.collectCardholderName,
                schemeConfig = config.schemeSupport,
                focusCardNumber = focusCardNumber,
                focusExpiry = focusExpiration,
                focusCode = focusCVV,
                enabled = uiState !is CardDetailsUIState.Loading && enabled,
                cardHolderName = inputState.cardholderName ?: "",
                cardNumber = inputState.cardNumber,
                expiry = inputState.expiry,
                code = inputState.code,
                cardScheme = inputState.cardScheme,
                onCardHolderNameChange = { viewModel.updateCardholderName(it) },
                onCardNumberChange = { viewModel.updateCardNumber(it) },
                onExpiryChange = { viewModel.updateExpiry(it) },
                onSecurityCodeChange = { viewModel.updateSecurityCode(it) }
            )

            // Save card toggle switch (if configured)
            if (config.allowSaveCard != null) {
                SaveCardToggle(
                    enabled = uiState !is CardDetailsUIState.Loading && enabled,
                    saveCard = inputState.saveCard,
                    config = config.allowSaveCard,
                    onToggle = viewModel::updateSaveCard
                )
            }

            // Submit button for tokenizing the card
            SdkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submitDetails"),
                text = config.actionText,
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
            completion(
                Result.success(
                    CardResult(
                        token = uiState.token,
                        saveCard = inputState.saveCard
                    )
                )
            )
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

@PreviewLightDark
@Composable
internal fun PreviewCardDetails() {
    SdkTheme {
        CardDetailsWidget(config = CardDetailsWidgetConfig(accessToken = "")) {

        }
    }
}