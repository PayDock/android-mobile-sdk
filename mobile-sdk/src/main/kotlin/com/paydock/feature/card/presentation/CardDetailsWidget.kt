package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.SdkButton
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.components.toggle.ToggleAppearance
import com.paydock.designsystems.components.toggle.ToggleAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
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
 * @param appearance Customization options for the visual appearance of the widget, encapsulated in [CardDetailsWidgetAppearance].
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
    appearance: CardDetailsWidgetAppearance = CardDetailsAppearanceDefaults.appearance(),
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
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appearance.verticalSpacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        // Title for the card information section
        if (config.showCardTitle) {
            SdkText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.label_card_information),
                appearance = appearance.title
            )
        }
        if (!config.schemeSupport.supportedSchemes.isNullOrEmpty()) {
            SupportedCardBanner(config.schemeSupport.supportedSchemes)
        }
        CardInputFields(
            shouldCollectCardholderName = config.collectCardholderName,
            schemeConfig = config.schemeSupport,
            verticalSpacing = appearance.verticalSpacing,
            horizontalSpacing = appearance.horizontalSpacing,
            textFieldAppearance = appearance.textField,
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

@Immutable
class CardDetailsWidgetAppearance(
    val verticalSpacing: Dp,
    val horizontalSpacing: Dp,
    val title: TextAppearance,
    val textField: TextFieldAppearance,
    val actionButton: @Composable (isEnabled: Boolean) -> ButtonAppearance,
    val switch: ToggleAppearance,
    val linkText: TextAppearance,
    val link: TextAppearance
) {
    /**
     * Creates a copy of this [CardDetailsWidgetAppearance] with optional overriding parameters.
     *
     * This function allows you to create a new [CardDetailsWidgetAppearance] instance
     * based on an existing one, while selectively changing certain appearance properties.
     * If a parameter is not explicitly provided, the corresponding value from the original
     * object is used.
     *
     * @param verticalSpacing The vertical spacing to use. Defaults to the original vertical spacing.
     * @param horizontalSpacing The horizontal spacing to use. Defaults to the original horizontal spacing.
     * @param title The text appearance for the title. Defaults to the original title appearance.
     * @param textField The appearance for text fields. Defaults to the original text field appearance.
     * @param actionButton A composable lambda that defines the appearance of the action button based on its enabled state.
     *   Defaults to the original action button appearance.
     * @param switch The appearance for the switch toggle. Defaults to the original switch appearance.
     * @param linkText The text appearance for non-interactive link text. Defaults to the original link text appearance.
     * @param link The text appearance for interactive links. Defaults to the original link appearance.
     * @return A new [CardDetailsWidgetAppearance] instance with the specified or default properties.
     */
    fun copy(
        verticalSpacing: Dp = this.verticalSpacing,
        horizontalSpacing: Dp = this.horizontalSpacing,
        title: TextAppearance = this.title,
        textField: TextFieldAppearance = this.textField,
        actionButton: @Composable (isEnabled: Boolean) -> ButtonAppearance = { isEnabled ->
            this.actionButton(isEnabled)
        },
        switch: ToggleAppearance = this.switch,
        linkText: TextAppearance = this.linkText,
        link: TextAppearance = this.link
    ): CardDetailsWidgetAppearance =
        CardDetailsWidgetAppearance(
            verticalSpacing = verticalSpacing.takeOrElse { this.verticalSpacing },
            horizontalSpacing = horizontalSpacing.takeOrElse { this.horizontalSpacing },
            title = title.copy(),
            textField = textField.copy(),
            actionButton = { isEnabled ->
                actionButton(isEnabled)
            },
            switch = switch.copy(),
            linkText = linkText.copy(),
            link = link.copy()
        )
}

/**
 * Default appearance settings for the CardDetailsWidget.
 *
 * This object provides a default [CardDetailsWidgetAppearance] configured with common styling
 * using Material Design typography and color schemes.
 *
 * You can use the [appearance] composable function to retrieve the default appearance.
 */
object CardDetailsAppearanceDefaults {

    /**
     * Defines the default appearance of the [CardDetailsWidget].
     *
     * This composable function provides a [CardDetailsWidgetAppearance] with default styling based on the current [MaterialTheme].
     * It specifies the spacing, text appearances for various elements (title, text fields, link text, links),
     * the appearance of the action button, and the appearance of the switch toggle.
     *
     * @return A [CardDetailsWidgetAppearance] instance configured with default styling.
     */
    @Composable
    fun appearance(): CardDetailsWidgetAppearance = CardDetailsWidgetAppearance(
        verticalSpacing = WidgetDefaults.SPACING,
        horizontalSpacing = WidgetDefaults.SPACING,
        title = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        ),
        textField = TextFieldAppearanceDefaults.appearance().copy(singleLine = true),
        actionButton = { isEnabled -> ButtonAppearanceDefaults.filledButtonAppearance(isEnabled) },
        switch = ToggleAppearanceDefaults.appearance(),
        linkText = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium,
        ),
        link = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value + 1).sp
            )
        )
    )
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

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardDetails() {
    CardDetailsWidget(config = CardDetailsWidgetConfig(accessToken = "")) {

    }
}