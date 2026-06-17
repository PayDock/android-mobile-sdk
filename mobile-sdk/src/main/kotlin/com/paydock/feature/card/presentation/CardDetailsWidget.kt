package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.domain.model.FormState
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.RenderButton
import com.paydock.designsystems.components.input.LocalSuppressFocusLossErrorAnnouncement
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.components.link.LinkTextAppearance
import com.paydock.designsystems.components.link.LinkTextAppearanceDefaults
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.components.toggle.ToggleAppearance
import com.paydock.designsystems.components.toggle.ToggleAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.card.domain.model.CardDetailsEventNames
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.presentation.components.CardInputFields
import com.paydock.feature.card.presentation.components.SaveCardToggle
import com.paydock.feature.card.presentation.components.SupportedCardBanner
import com.paydock.feature.card.presentation.state.CardDetailsInputState
import com.paydock.feature.card.presentation.state.CardDetailsInputState.CardField
import com.paydock.feature.card.presentation.state.CardDetailsUIState
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * @param eventDelegate An optional [WidgetEventDelegate] for tracking widget events such as
 * button clicks, toggle interactions, and link clicks.
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
    eventDelegate: WidgetEventDelegate? = null,
    completion: (Result<CardResult>) -> Unit
) {
    val viewModel: CardDetailsViewModel = koinViewModel(parameters = {
        parametersOf(config.accessToken, config.gatewayId, config.schemeSupport)
    })
    viewModel.setCollectCardholderName(config.collectCardholderName)
    viewModel.setStoreSecurityCode(config.storeSecurityCode)
    val inputState by viewModel.inputStateFlow.collectAsState()
    val uiState by viewModel.stateFlow.collectAsState()
    val isDataValid by remember(uiState) { derivedStateOf { inputState.isDataValid } }

    val isEnabled by remember(uiState) {
        derivedStateOf {
            val isDataValidIfRequired = if (config.activePrimaryButton) true else isDataValid
            isDataValidIfRequired && uiState !is CardDetailsUIState.Loading && enabled
        }
    }
    val isLoading by remember(uiState) {
        derivedStateOf { loadingDelegate == null && uiState is CardDetailsUIState.Loading }
    }

    val focusCardNumber = remember { FocusRequester() }
    val focusExpiration = remember { FocusRequester() }
    val focusCVV = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var focusCardholderNameA11y by remember { mutableStateOf(false) }
    var focusCardNumberA11y by remember { mutableStateOf(false) }
    var focusExpiryA11y by remember { mutableStateOf(false) }
    var focusSecurityCodeA11y by remember { mutableStateOf(false) }
    var errorAnnouncement by remember { mutableStateOf<String?>(null) }
    // While true, fields suppress their own focus-loss error announcement so submit's clearFocus
    // doesn't announce the edited field's error on top of the error-count + first-error-field
    // announcements below. Auto-reset shortly after, so normal defocus announcements resume.
    var suppressFieldFocusLossAnnouncement by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Handles UI state changes (success or failure of tokenization)
    LaunchedEffect(uiState) {
        handleUIState(uiState, inputState, viewModel, loadingDelegate, completion)
    }

    // Add a reset effect for each so they can be re-triggered on subsequent submits
    LaunchedEffect(focusCardholderNameA11y) {
        if (focusCardholderNameA11y) {
            // Give the accessibility framework time to readout, if this fire before readout complete,
            // it will reannounce state change
            delay(20000)
            focusCardholderNameA11y = false
        }
    }
    LaunchedEffect(focusCardNumberA11y) {
        if (focusCardNumberA11y) {
            // Give the accessibility framework time to readout, if this fire before readout complete,
            // it will reannounce state change
            delay(20000)
            focusCardNumberA11y = false
        }
    }
    LaunchedEffect(focusExpiryA11y) {
        if (focusExpiryA11y) {
            // Give the accessibility framework time to readout, if this fire before readout complete,
            // it will reannounce state change
            delay(20000)
            focusExpiryA11y = false
        }
    }
    LaunchedEffect(focusSecurityCodeA11y) {
        if (focusSecurityCodeA11y) {
            // Give the accessibility framework time to readout, if this fire before readout complete,
            // it will reannounce state change
            delay(20000)
            focusSecurityCodeA11y = false
        }
    }
    LaunchedEffect(errorAnnouncement) {
        if (errorAnnouncement != null) {
            delay(2000) // Give the accessibility framework a moment to register the event
            errorAnnouncement = null // Reset it to null
        }
    }
    LaunchedEffect(suppressFieldFocusLossAnnouncement) {
        if (suppressFieldFocusLossAnnouncement) {
            // Cover the recomposition where clearFocus drops the edited field's focus, then resume
            // normal per-field defocus announcements.
            delay(1500)
            suppressFieldFocusLossAnnouncement = false
        }
    }

    // UI Layout starts here
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appearance.verticalSpacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        // Show card scheme icons: if null (allow all) or empty set, show all schemes; otherwise show selected schemes
        val schemesToDisplay = when {
            config.schemeSupport.supportedSchemes.isNullOrEmpty() -> CardType.entries.toSet()
            else -> config.schemeSupport.supportedSchemes!!
        }
        SupportedCardBanner(schemesToDisplay)
        // Provide the focus-loss suppression flag to the input fields so submit's clearFocus
        // doesn't trigger a per-field error announcement (see suppressFieldFocusLossAnnouncement).
        CompositionLocalProvider(
            LocalSuppressFocusLossErrorAnnouncement provides suppressFieldFocusLossAnnouncement
        ) {
            CardInputFields(
                shouldCollectCardholderName = config.collectCardholderName,
                schemeConfig = config.schemeSupport,
                verticalSpacing = appearance.textFieldVerticalSpacing,
                horizontalSpacing = appearance.textFieldHorizontalSpacing,
                cardNameTextFieldAppearance = appearance.cardNameTextField,
                cardNumberTextFieldAppearance = appearance.cardNumberTextField,
                expiryTextFieldAppearance = appearance.cardExpiryTextField,
                securityCodeTextFieldAppearance = appearance.cardSecurityCodeTextField,
                focusCardNumber = focusCardNumber,
                focusExpiry = focusExpiration,
                focusCode = focusCVV,
                a11yCardNameFocus = focusCardholderNameA11y,
                a11yCardNumberFocus = focusCardNumberA11y,
                a11yExpiryFocus = focusExpiryA11y,
                a11ySecurityCodeFocus = focusSecurityCodeA11y,
                enabled = uiState !is CardDetailsUIState.Loading && enabled,
                cardHolderName = inputState.cardholderName ?: "",
                cardNumber = inputState.cardNumber,
                expiry = inputState.expiry,
                code = inputState.code,
                cardScheme = inputState.cardScheme,
                cardholderNameErrorOverwrite = inputState.cardholderNameErrorOverwrite,
                cardNumberErrorOverwrite = inputState.cardNumberErrorOverwrite,
                cardExpiryErrorOverwrite = inputState.cardExpiryErrorOverwrite,
                cardSecurityErrorOverwrite = inputState.cardSecurityErrorOverwrite,
                onCardHolderNameChange = { viewModel.updateCardholderName(it) },
                onCardNumberChange = { viewModel.updateCardNumber(it) },
                onExpiryChange = { viewModel.updateExpiry(it) },
                onSecurityCodeChange = { viewModel.updateSecurityCode(it) }
            )
        }

        // Save card toggle switch (if configured)
        if (config.allowSaveCard != null && config.allowSaveCard.isValid()) {
            SaveCardToggle(
                enabled = uiState !is CardDetailsUIState.Loading && enabled,
                saveCard = inputState.saveCard,
                config = config.allowSaveCard,
                linkTextAppearance = appearance.linkText,
                linkToggleAppearance = appearance.toggleText,
                toggleAppearance = appearance.toggle,
                onToggle = { newState ->
                    viewModel.updateSaveCard(newState)
                    // Emit toggle event
                    eventDelegate?.widgetEvent(
                        Event.ToggleEvent(
                            name = CardDetailsEventNames.SAVE_CARD_TOGGLE,
                            action = EventAction.CLICK,
                            state = newState
                        )
                    )
                },
                onPrivacyPolicyClick = { url ->
                    // Emit link event
                    eventDelegate?.widgetEvent(
                        Event.LinkTextEvent(
                            name = CardDetailsEventNames.PRIVACY_POLICY_LINK,
                            action = EventAction.CLICK,
                            url = url
                        )
                    )
                }
            )
        }

        Column {
            val errorCountMessage = if (inputState.errorCount > 1) {
                stringResource(R.string.error_form_count_plural, inputState.errorCount)
            } else if (inputState.errorCount == 1) {
                stringResource(R.string.error_form_count_singular, inputState.errorCount)
            } else {
                ""
            }
            if (errorAnnouncement != null) {
                // Invisible text used for screen reader announcement via live region
                androidx.compose.material3.Text(
                    text = errorAnnouncement!!,
                    modifier = Modifier
                        .size(1.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                )
            }
            appearance.actionButton.RenderButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submitDetails"),
                text = appearance.actionButton.text,
                buttonIcon = appearance.actionButton.icon,
                enabled = isEnabled,
                isLoading = isLoading,
            ) {
                // Suppress the edited field's focus-loss error announcement before clearing focus,
                // so it doesn't talk over the error-count and first-error-field announcements below.
                suppressFieldFocusLossAnnouncement = true
                focusManager.clearFocus()

                // Emit button event
                eventDelegate?.widgetEvent(
                    Event.ButtonEvent(
                        name = CardDetailsEventNames.TOKENISATION_BUTTON,
                        action = EventAction.CLICK,
                        text = appearance.actionButton.text,
                        formState = if (isDataValid) FormState.VALID else FormState.INVALID
                    )
                )

                if (config.activePrimaryButton && !isDataValid) {
                    // If the button was enabled by default but data is not valid,
                    // we should trigger validation to show errors.
                    viewModel.validateAllFields()

                    // Update error announcement to trigger a screen reader announcement via live region
                    errorAnnouncement = errorCountMessage

                    coroutineScope.launch {
                        // Reset if used before
                        if (focusCardholderNameA11y) { focusCardholderNameA11y = false }
                        if (focusCardNumberA11y) { focusCardNumberA11y = false }
                        if (focusExpiryA11y) { focusExpiryA11y = false }
                        if (focusSecurityCodeA11y) { focusSecurityCodeA11y = false }

                        delay(1000)

                        // Shift focus to the first invalid field
                        val firstInvalidField = inputState.invalidFields.firstOrNull()

                        when (firstInvalidField) {
                            CardField.CARDHOLDER_NAME -> focusCardholderNameA11y = true
                            CardField.CARD_NUMBER -> focusCardNumberA11y = true
                            CardField.EXPIRY -> focusExpiryA11y = true
                            CardField.SECURITY_CODE -> focusSecurityCodeA11y = true
                            null -> Unit
                        }
                    }
                } else {
                    errorAnnouncement = null
                    viewModel.tokeniseCard()
                }
            }
        }
    }
}

/**
 * Represents the appearance settings for the Card Details Widget.
 *
 * This class defines the visual styling for various components within the
 * [CardDetailsWidget], such as spacing, text appearances, button appearance,
 * and toggle appearance.
 *
 * @property verticalSpacing The vertical spacing between elements in the widget.
 * @property horizontalSpacing The horizontal spacing within composite elements (e.g., text fields).
 * @property textFieldVerticalSpacing The vertical spacing between text input fields.
 * @property textFieldHorizontalSpacing The horizontal spacing between text input fields.
 * @property cardNameTextField The appearance settings for the Card Name text input field.
 * @property cardNumberTextField The appearance settings for the Card Number text input field.
 * @property cardExpiryTextField The appearance settings for the Card Expiry text input field.
 * @property cardSecurityCodeTextField The appearance settings for the Card Security text input field.
 * @property actionButton A composable lambda that provides the [ButtonAppearance] based on whether the button is enabled.
 * @property toggle The appearance settings for the toggle switch (e.g., save card option).
 * @property toggleText The text appearance for non-interactive link-style text.
 * @property linkText The text appearance for interactive link elements.
 */
@Immutable
class CardDetailsWidgetAppearance(
    val verticalSpacing: Dp,
    val horizontalSpacing: Dp,
    val textFieldVerticalSpacing: Dp,
    val textFieldHorizontalSpacing: Dp,
    val cardNameTextField: TextFieldAppearance,
    val cardNumberTextField: TextFieldAppearance,
    val cardExpiryTextField: TextFieldAppearance,
    val cardSecurityCodeTextField: TextFieldAppearance,
    val actionButton: ButtonAppearance,
    val toggle: ToggleAppearance,
    val toggleText: TextAppearance,
    val linkText: LinkTextAppearance
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
     * @param textFieldVerticalSpacing The vertical spacing between text fields. Defaults to the original text field vertical spacing.
     * @param textFieldHorizontalSpacing The horizontal spacing between text fields. Defaults to the original text field horizontal spacing.
     * @param cardNameTextField The appearance for the card name text field. Defaults to the original text field appearance.
     * @param cardNumberTextField The appearance for the card number text field. Defaults to the original text field appearance.
     * @param cardExpiryTextField The appearance for the card expiry text field. Defaults to the original text field appearance.
     * @param cardSecurityCodeTextField The appearance for the card security code text field. Defaults to the original text field appearance.
     * @param actionButton A composable lambda that defines the appearance of the action button based on its enabled state.
     *   Defaults to the original action button appearance.
     * @param switch The appearance for the switch toggle. Defaults to the original switch appearance.
     * @param toggleText The text appearance for non-interactive link text. Defaults to the original link text appearance.
     * @param linkText The text appearance for interactive links. Defaults to the original link appearance.
     * @return A new [CardDetailsWidgetAppearance] instance with the specified or default properties.
     */
    fun copy(
        verticalSpacing: Dp = this.verticalSpacing,
        horizontalSpacing: Dp = this.horizontalSpacing,
        textFieldVerticalSpacing: Dp = this.textFieldVerticalSpacing,
        textFieldHorizontalSpacing: Dp = this.textFieldHorizontalSpacing,
        cardNameTextField: TextFieldAppearance = this.cardNameTextField,
        cardNumberTextField: TextFieldAppearance = this.cardNumberTextField,
        cardExpiryTextField: TextFieldAppearance = this.cardExpiryTextField,
        cardSecurityCodeTextField: TextFieldAppearance = this.cardSecurityCodeTextField,
        actionButton: ButtonAppearance = this.actionButton,
        switch: ToggleAppearance = this.toggle,
        toggleText: TextAppearance = this.toggleText,
        linkText: LinkTextAppearance = this.linkText
    ): CardDetailsWidgetAppearance =
        CardDetailsWidgetAppearance(
            verticalSpacing = verticalSpacing.takeOrElse { this.verticalSpacing },
            horizontalSpacing = horizontalSpacing.takeOrElse { this.horizontalSpacing },
            textFieldVerticalSpacing = textFieldVerticalSpacing.takeOrElse { this.textFieldVerticalSpacing },
            textFieldHorizontalSpacing = textFieldHorizontalSpacing.takeOrElse { this.textFieldHorizontalSpacing },
            cardNameTextField = cardNameTextField.copy(),
            cardNumberTextField = cardNumberTextField.copy(),
            cardExpiryTextField = cardExpiryTextField.copy(),
            cardSecurityCodeTextField = cardSecurityCodeTextField.copy(),
            actionButton = when (actionButton) {
                is ButtonAppearance.FilledButtonAppearance -> actionButton.copy()
                is ButtonAppearance.IconButtonAppearance -> actionButton.copy()
                is ButtonAppearance.OutlineButtonAppearance -> actionButton.copy()
                is ButtonAppearance.TextButtonAppearance -> actionButton.copy()
            },
            toggle = switch.copy(),
            toggleText = toggleText.copy(),
            linkText = linkText.copy()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CardDetailsWidgetAppearance

        if (verticalSpacing != other.verticalSpacing) return false
        if (horizontalSpacing != other.horizontalSpacing) return false
        if (textFieldVerticalSpacing != other.textFieldVerticalSpacing) return false
        if (textFieldHorizontalSpacing != other.textFieldHorizontalSpacing) return false
        if (cardNameTextField != other.cardNameTextField) return false
        if (cardNumberTextField != other.cardNumberTextField) return false
        if (cardExpiryTextField != other.cardExpiryTextField) return false
        if (cardSecurityCodeTextField != other.cardSecurityCodeTextField) return false
        if (actionButton != other.actionButton) return false
        if (toggle != other.toggle) return false
        if (toggleText != other.toggleText) return false
        if (linkText != other.linkText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = verticalSpacing.hashCode()
        result = 31 * result + horizontalSpacing.hashCode()
        result = 31 * result + textFieldVerticalSpacing.hashCode()
        result = 31 * result + textFieldHorizontalSpacing.hashCode()
        result = 31 * result + cardNameTextField.hashCode()
        result = 31 * result + cardNumberTextField.hashCode()
        result = 31 * result + cardExpiryTextField.hashCode()
        result = 31 * result + cardSecurityCodeTextField.hashCode()
        result = 31 * result + actionButton.hashCode()
        result = 31 * result + toggle.hashCode()
        result = 31 * result + toggleText.hashCode()
        result = 31 * result + linkText.hashCode()
        return result
    }
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
        verticalSpacing = WidgetDefaults.Spacing,
        horizontalSpacing = WidgetDefaults.Spacing,
        textFieldVerticalSpacing = WidgetDefaults.Spacing,
        textFieldHorizontalSpacing = WidgetDefaults.Spacing,
        // Seed the default placeholder/hint strings into the appearance so they
        // are discoverable and editable via the appearance object (e.g. shown in the styling
        // screen) rather than living only as fallbacks inside each input component.
        // The security code placeholder "XXX" is treated as the default mask by
        // CardSecurityCodeInput, which still widens it to "XXXX" for Amex.
        cardNameTextField = TextFieldAppearanceDefaults.appearance().copy(
            singleLine = true,
            hintText = stringResource(R.string.hint_card_name)
        ),
        cardNumberTextField = TextFieldAppearanceDefaults.appearance().copy(
            singleLine = true,
            placeholderText = stringResource(R.string.placeholder_card_number),
            hintText = stringResource(R.string.hint_card_number)
        ),
        cardExpiryTextField = TextFieldAppearanceDefaults.appearance().copy(
            singleLine = true,
            placeholderText = stringResource(R.string.placeholder_expiry),
            hintText = stringResource(R.string.hint_expiry_date)
        ),
        cardSecurityCodeTextField = TextFieldAppearanceDefaults.appearance().copy(
            singleLine = true,
            placeholderText = "XXX",
            hintText = stringResource(R.string.hint_cvv)
        ),
        actionButton = ButtonAppearanceDefaults.filledButtonAppearance().copy(
            text = stringResource(R.string.button_submit),
            clickableDescription = stringResource(R.string.button_submit)
        ),
        toggle = ToggleAppearanceDefaults.appearance(),
        toggleText = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium,
        ),
        linkText = LinkTextAppearanceDefaults.appearance()
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
    CardDetailsWidget(
        config = CardDetailsWidgetConfig(accessToken = ""),
        completion = {}
    )
}