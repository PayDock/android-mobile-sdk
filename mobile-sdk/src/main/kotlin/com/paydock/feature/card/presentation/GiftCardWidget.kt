package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
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
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.RenderButton
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.card.domain.model.GiftCardEventNames
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.presentation.components.CardPinInput
import com.paydock.feature.card.presentation.components.GiftCardNumberInput
import com.paydock.feature.card.presentation.state.GiftCardInputState.GiftCardField
import com.paydock.feature.card.presentation.state.GiftCardUIState
import com.paydock.feature.card.presentation.state.GiftCardWidgetState
import com.paydock.feature.card.presentation.state.rememberGiftCardWidgetState
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * @param config The configuration for the gift card widget, including necessary access details.
 * @param appearance Defines the visual appearance of the gift card widget elements. Defaults to a standard appearance.
 * @param loadingDelegate An optional delegate to manage the visibility of loading indicators externally.
 * @param eventDelegate An optional [WidgetEventDelegate] for tracking widget events such as button clicks.
 * @param state A [GiftCardWidgetState] (see [rememberGiftCardWidgetState]) used to drive submission
 * from outside the widget when `config.showSubmitButton` is `false` — call `state.submit()` from your
 * own button. See [GiftCardWidgetState] for how to derive your own button's enabled state.
 * @param completion A callback invoked with the result of the tokenization process, providing either
 *                   a success with the token or a failure with an exception.
 */
@Composable
fun GiftCardWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: GiftCardWidgetConfig,
    appearance: GiftCardWidgetAppearance = GiftCardAppearanceDefaults.appearance(),
    loadingDelegate: WidgetLoadingDelegate? = null,
    eventDelegate: WidgetEventDelegate? = null,
    state: GiftCardWidgetState = rememberGiftCardWidgetState(),
    completion: (Result<String>) -> Unit,
) {
    // ViewModel instance scoped to the Koin dependency injection framework
    val viewModel: GiftCardViewModel = koinViewModel(parameters = { parametersOf(config) })

    // Observing state flows for input and UI state
    val inputState by viewModel.inputStateFlow.collectAsState()
    val uiState by viewModel.stateFlow.collectAsState()

    val isDataValid by remember(uiState) { derivedStateOf { inputState.isDataValid } }
    val isEnabled by remember(uiState) {
        derivedStateOf {
            val isDataValidIfRequired = if (config.activePrimaryButton) true else isDataValid
            isDataValidIfRequired && uiState !is GiftCardUIState.Loading && enabled
        }
    }
    val isLoading by remember(uiState) { derivedStateOf { loadingDelegate == null && uiState is GiftCardUIState.Loading } }

    // Publish live validity to the external state handle, for hosts using
    // `config.showSubmitButton = false` to drive their own submit UI (see GiftCardWidgetState).
    SideEffect {
        state.isFormValid = isDataValid
    }

    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    // Resolve per-field appearance, falling back to the shared textField when no override is provided.
    val cardNumberAppearance = appearance.cardNumberTextField ?: appearance.textField
    val pinAppearance = appearance.pinTextField ?: appearance.textField

    // Focus handlers for input fields
    val focusCardNumber = remember { FocusRequester() }
    val focusCardPin = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var focusCardNumberA11y by remember { mutableStateOf(false) }
    var focusPinA11y by remember { mutableStateOf(false) }
    var errorAnnouncement by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val errorCountMessage = when {
        inputState.errorCount > 1 -> stringResource(R.string.error_form_count_plural, inputState.errorCount)
        inputState.errorCount == 1 -> stringResource(R.string.error_form_count_singular, inputState.errorCount)
        else -> ""
    }

    // Validates and, if valid, tokenises the gift card. Shared by the internal Add button and the
    // external `state.submit()` trigger, so both entry points behave identically — see
    // GiftCardWidgetConfig.showSubmitButton.
    val submitTapped: () -> Unit = submitTapped@{
        // The external trigger bypasses the button's own `enabled = isEnabled` guard (loading state
        // and the widget's `enabled` param), so both must be re-checked here.
        if (uiState is GiftCardUIState.Loading || !enabled) return@submitTapped

        focusManager.clearFocus()

        // Emit button event
        eventDelegate?.widgetEvent(
            Event.ButtonEvent(
                name = GiftCardEventNames.TOKENISATION_BUTTON,
                action = EventAction.CLICK,
                text = appearance.actionButton.text
            )
        )

        if (!isDataValid) {
            // Data is not valid (regardless of whether the internal button was enabled to reach
            // here): trigger validation to surface errors rather than tokenising invalid input.
            viewModel.validateAllFields()

            // Announce the aggregate error count via the live region.
            errorAnnouncement = errorCountMessage

            coroutineScope.launch {
                if (focusCardNumberA11y) focusCardNumberA11y = false
                if (focusPinA11y) focusPinA11y = false

                delay(FIRST_ERROR_FOCUS_DELAY)

                // Shift accessibility focus to the first invalid field.
                when (inputState.invalidFields.firstOrNull()) {
                    GiftCardField.CARD_NUMBER -> focusCardNumberA11y = true
                    GiftCardField.PIN -> focusPinA11y = true
                    null -> Unit
                }
            }
        } else {
            errorAnnouncement = null
            viewModel.tokeniseCard()
        }
    }

    // React to changes in the UI state
    LaunchedEffect(uiState) {
        handleUIState(uiState, viewModel, loadingDelegate, completion)
    }

    // Wires the external submit trigger to the identical submit logic the internal button uses.
    // `LaunchedEffect(state)` only re-launches when `state` itself changes (never, in practice), so
    // the collector must read `submitTapped` through `rememberUpdatedState` rather than closing over
    // it directly — otherwise it would keep calling composition-0's lambda (with composition-0's
    // captured `errorCountMessage`/`config`/`appearance`) for the widget's whole lifetime.
    val currentSubmitTapped by rememberUpdatedState(submitTapped)
    LaunchedEffect(state) {
        state.submitRequests.collect { currentSubmitTapped() }
    }

    // Reset a11y focus flags shortly after firing so they can be re-triggered on subsequent submits.
    LaunchedEffect(focusCardNumberA11y) {
        if (focusCardNumberA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusCardNumberA11y = false
        }
    }
    LaunchedEffect(focusPinA11y) {
        if (focusPinA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusPinA11y = false
        }
    }
    LaunchedEffect(errorAnnouncement) {
        if (errorAnnouncement != null) {
            delay(ERROR_ANNOUNCEMENT_RESET_DELAY)
            errorAnnouncement = null
        }
    }

    // Composing the UI
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appearance.verticalSpacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        // Use BoxWithConstraints to measure available width and make intelligent layout decision
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // For GiftCard: Card Number (70% weight) + PIN (30% weight) + spacing
            // PIN field (smaller field) needs ~80dp minimum for 4 digits + padding
            val shouldUseColumnLayout = WidgetDefaults.shouldUseColumnLayout(
                availableWidth = maxWidth,
                fontScale = fontScale,
                minFieldWidth = 80.dp, // PIN field minimum
                fieldWeight = 0.3f // PIN field weight
            )

            if (shouldUseColumnLayout) {
                CardNumberPinColumn(
                    verticalSpacing = appearance.textFieldVerticalSpacing,
                    cardNumberAppearance = cardNumberAppearance,
                    pinAppearance = pinAppearance,
                    enabled = uiState !is GiftCardUIState.Loading && enabled,
                    cardNumber = inputState.cardNumber,
                    cardPin = inputState.pin,
                    focusCardNumber = focusCardNumber,
                    focusCardPin = focusCardPin,
                    cardNumberForceShowErrors = inputState.cardNumberErrorOverwrite,
                    pinForceShowErrors = inputState.pinErrorOverwrite,
                    a11yCardNumberFocus = focusCardNumberA11y,
                    a11yPinFocus = focusPinA11y,
                    onCardNumberChange = { viewModel.updateCardNumber(it) },
                    onPinChange = { viewModel.updateCardPin(it) }
                )
            } else {
                CardNumberPinRow(
                    horizontalSpacing = appearance.textFieldHorizontalSpacing,
                    cardNumberAppearance = cardNumberAppearance,
                    pinAppearance = pinAppearance,
                    enabled = uiState !is GiftCardUIState.Loading && enabled,
                    cardNumber = inputState.cardNumber,
                    cardPin = inputState.pin,
                    focusCardNumber = focusCardNumber,
                    focusCardPin = focusCardPin,
                    cardNumberForceShowErrors = inputState.cardNumberErrorOverwrite,
                    pinForceShowErrors = inputState.pinErrorOverwrite,
                    a11yCardNumberFocus = focusCardNumberA11y,
                    a11yPinFocus = focusPinA11y,
                    onCardNumberChange = { viewModel.updateCardNumber(it) },
                    onPinChange = { viewModel.updateCardPin(it) }
                )
            }
        }

        Column {
            if (errorAnnouncement != null) {
                // Invisible text used for screen reader announcement via live region
                Text(
                    text = errorAnnouncement!!,
                    modifier = Modifier
                        .size(1.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            // Submit button
            if (config.showSubmitButton) {
                appearance.actionButton.RenderButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("addCard"),
                    text = appearance.actionButton.text,
                    buttonIcon = appearance.actionButton.icon,
                    enabled = isEnabled,
                    isLoading = isLoading,
                ) {
                    submitTapped()
                }
            }
        }
    }
}

// Give the accessibility framework time to read out before resetting the focus flag; firing again
// before readout completes would re-announce the state change.
private const val A11Y_FOCUS_RESET_DELAY = 20000L
private const val ERROR_ANNOUNCEMENT_RESET_DELAY = 2000L
private const val FIRST_ERROR_FOCUS_DELAY = 1000L

/**
 * Displays the card number and PIN input fields in a horizontal row.
 *
 * This composable is used when there is enough horizontal space to display both fields side-by-side.
 * It leverages [GiftCardNumberInput] and [CardPinInput] for the respective fields.
 *
 * @param horizontalSpacing The spacing between the card number and PIN input fields.
 * @param appearance The visual appearance configuration for the text fields.
 * @param enabled Whether the input fields are enabled for user interaction.
 * @param cardNumber The current value of the card number input.
 * @param cardPin The current value of the PIN input.
 * @param focusCardNumber The [FocusRequester] for the card number input field.
 * @param focusCardPin The [FocusRequester] for the PIN input field.
 * @param onCardNumberChange Callback invoked when the card number input value changes.
 * @param onPinChange Callback invoked when the PIN input value changes.
 */
@Composable
fun CardNumberPinRow(
    horizontalSpacing: Dp,
    cardNumberAppearance: TextFieldAppearance,
    pinAppearance: TextFieldAppearance,
    enabled: Boolean,
    cardNumber: String,
    cardPin: String,
    focusCardNumber: FocusRequester,
    focusCardPin: FocusRequester,
    cardNumberForceShowErrors: Boolean = false,
    pinForceShowErrors: Boolean = false,
    a11yCardNumberFocus: Boolean = false,
    a11yPinFocus: Boolean = false,
    onCardNumberChange: (String) -> Unit,
    onPinChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            horizontalSpacing,
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
            appearance = cardNumberAppearance,
            value = cardNumber,
            enabled = enabled,
            forceShowErrors = cardNumberForceShowErrors,
            a11yFocus = a11yCardNumberFocus,
            onValueChange = onCardNumberChange,
            nextFocus = focusCardPin
        )

        // PIN input field
        CardPinInput(
            modifier = Modifier
                .weight(0.3f)
                .focusRequester(focusCardPin)
                .testTag("cardPinInput"),
            appearance = pinAppearance,
            value = cardPin,
            enabled = enabled,
            forceShowErrors = pinForceShowErrors,
            a11yFocus = a11yPinFocus,
            onValueChange = onPinChange
        )
    }
}

/**
 * Displays the card number and PIN input fields in a vertical column.
 *
 * This composable is used when there isn't enough horizontal space to display both fields
 * side-by-side, such as on smaller screens or when the font scale is large.
 * It leverages [GiftCardNumberInput] and [CardPinInput] for the respective fields,
 * arranging them one above the other.
 *
 * @param verticalSpacing The vertical spacing between the card number and PIN input fields.
 * @param appearance The visual appearance configuration for the text fields.
 * @param enabled Whether the input fields are enabled for user interaction.
 * @param cardNumber The current value of the card number input.
 * @param cardPin The current value of the PIN input.
 * @param focusCardNumber The [FocusRequester] for the card number input field.
 * @param focusCardPin The [FocusRequester] for the PIN input field.
 * @param onCardNumberChange Callback invoked when the card number input value changes.
 * @param onPinChange Callback invoked when the PIN input value changes.
 */
@Composable
fun CardNumberPinColumn(
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    cardNumberAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    pinAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    enabled: Boolean,
    cardNumber: String,
    cardPin: String,
    focusCardNumber: FocusRequester = FocusRequester(),
    focusCardPin: FocusRequester = FocusRequester(),
    cardNumberForceShowErrors: Boolean = false,
    pinForceShowErrors: Boolean = false,
    a11yCardNumberFocus: Boolean = false,
    a11yPinFocus: Boolean = false,
    onCardNumberChange: (String) -> Unit = {},
    onPinChange: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.Start
    ) {
        // Card number input field
        GiftCardNumberInput(
            modifier = Modifier
                .focusRequester(focusCardNumber)
                .testTag("cardNumberInput"),
            appearance = cardNumberAppearance,
            value = cardNumber,
            enabled = enabled,
            forceShowErrors = cardNumberForceShowErrors,
            a11yFocus = a11yCardNumberFocus,
            onValueChange = onCardNumberChange,
            nextFocus = focusCardPin
        )

        // PIN input field
        CardPinInput(
            modifier = Modifier
                .focusRequester(focusCardPin)
                .testTag("cardPinInput"),
            appearance = pinAppearance,
            value = cardPin,
            enabled = enabled,
            forceShowErrors = pinForceShowErrors,
            a11yFocus = a11yPinFocus,
            onValueChange = onPinChange
        )
    }
}

/**
 * Represents the visual appearance configuration for a [GiftCardWidget].
 *
 * This class encapsulates the styling and layout properties that define the appearance of a
 * [GiftCardWidget], including the visual style of its sub-components (text fields, action button),
 * and the internal spacing between them. It offers a structured way to customize the widget's look
 * and feel, ensuring consistency across the application.
 *
 * @property verticalSpacing The vertical space between elements within the [GiftCardWidget], such as
 *  the row of input fields and the action button. This property controls the amount of padding or
 *  margin applied vertically between these elements.
 * @property horizontalSpacing The horizontal space between elements within the row of input fields in
 *  the [GiftCardWidget]. This affects the spacing between the card number and PIN input fields.
 * @property textFieldVerticalSpacing The vertical spacing between text input fields.
 * @property textFieldHorizontalSpacing The horizontal spacing between text input fields.
 * @property textField The appearance configuration for the input text fields within the
 *  [GiftCardWidget]. This property allows for customization of the text field's visual style, such
 *  as the colors, borders, and content padding. See [TextFieldAppearance] for more details.
 * @property actionButton A composable lambda that defines the appearance of the primary action
 *  button within the [GiftCardWidget]. It takes a boolean parameter `isEnabled` to indicate whether
 *  the button is currently enabled. This allows for dynamic styling of the button based on its state,
 *  such as changing its color or text based on whether it's clickable. The lambda should return a
 *  [ButtonAppearance] object, which further defines the button's style.
 *
 * @see GiftCardWidget
 * @see TextFieldAppearance
 * @see ButtonAppearance
 */
@Immutable
class GiftCardWidgetAppearance(
    val verticalSpacing: Dp,
    val horizontalSpacing: Dp,
    val textFieldVerticalSpacing: Dp,
    val textFieldHorizontalSpacing: Dp,
    val textField: TextFieldAppearance,
    val actionButton: ButtonAppearance,
    // Optional per-field overrides. When null, the field falls back to [textField].
    val cardNumberTextField: TextFieldAppearance? = null,
    val pinTextField: TextFieldAppearance? = null,
) {
    /**
     * Creates a new [GiftCardWidgetAppearance] instance with optional overrides.
     *
     * This function allows for creating a modified copy of the current [GiftCardWidgetAppearance] object,
     * selectively overriding specific properties while retaining the original values of others. It is
     * commonly used to create variations of the [GiftCardWidget]'s appearance without altering the
     * original configuration.
     *
     * @param verticalSpacing An optional override for the vertical spacing between elements. If set to
     *  [Dp.Unspecified], the original `verticalSpacing` of this instance will be used.
     * @param horizontalSpacing An optional override for the horizontal spacing between elements. If set
     *  to [Dp.Unspecified], the original `horizontalSpacing` of this instance will be used.
     * @param textFieldVerticalSpacing An optional override for the vertical spacing between text fields. If set to
     *  [Dp.Unspecified], the original `textFieldVerticalSpacing` of this instance will be used.
     * @param textFieldHorizontalSpacing An optional override for the horizontal spacing between text fields. If set to
     *  [Dp.Unspecified], the original `textFieldHorizontalSpacing` of this instance will be used.
     * @param textField An optional override for the text field appearance configuration. If not
     *  specified, the original `textField` appearance will be used.
     * @param actionButton An optional override for the action button's appearance. If not specified, the
     *  original `actionButton` configuration will be used. It provides a default implementation using
     *  [ButtonAppearanceDefaults.filledButtonAppearance] which dynamically updates the button style based
     *  on the enabled state.
     *
     * @return A new [GiftCardWidgetAppearance] instance with the specified overrides applied.
     */
    fun copy(
        verticalSpacing: Dp = this.verticalSpacing,
        horizontalSpacing: Dp = this.horizontalSpacing,
        textFieldVerticalSpacing: Dp = this.textFieldVerticalSpacing,
        textFieldHorizontalSpacing: Dp = this.textFieldHorizontalSpacing,
        textField: TextFieldAppearance = this.textField,
        actionButton: ButtonAppearance = this.actionButton,
        cardNumberTextField: TextFieldAppearance? = this.cardNumberTextField,
        pinTextField: TextFieldAppearance? = this.pinTextField,
    ): GiftCardWidgetAppearance = GiftCardWidgetAppearance(
        verticalSpacing = verticalSpacing.takeOrElse { this.verticalSpacing },
        horizontalSpacing = horizontalSpacing.takeOrElse { this.horizontalSpacing },
        textFieldVerticalSpacing = textFieldVerticalSpacing.takeOrElse { this.textFieldVerticalSpacing },
        textFieldHorizontalSpacing = textFieldHorizontalSpacing.takeOrElse { this.textFieldHorizontalSpacing },
        textField = textField.copy(),
        actionButton = when (actionButton) {
            is ButtonAppearance.FilledButtonAppearance -> actionButton.copy()
            is ButtonAppearance.IconButtonAppearance -> actionButton.copy()
            is ButtonAppearance.OutlineButtonAppearance -> actionButton.copy()
            is ButtonAppearance.TextButtonAppearance -> actionButton.copy()
        },
        cardNumberTextField = cardNumberTextField?.copy(),
        pinTextField = pinTextField?.copy(),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiftCardWidgetAppearance

        if (verticalSpacing != other.verticalSpacing) return false
        if (horizontalSpacing != other.horizontalSpacing) return false
        if (textFieldVerticalSpacing != other.textFieldVerticalSpacing) return false
        if (textFieldHorizontalSpacing != other.textFieldHorizontalSpacing) return false
        if (textField != other.textField) return false
        if (actionButton != other.actionButton) return false
        if (cardNumberTextField != other.cardNumberTextField) return false
        if (pinTextField != other.pinTextField) return false

        return true
    }

    override fun hashCode(): Int {
        var result = verticalSpacing.hashCode()
        result = 31 * result + horizontalSpacing.hashCode()
        result = 31 * result + textFieldVerticalSpacing.hashCode()
        result = 31 * result + textFieldHorizontalSpacing.hashCode()
        result = 31 * result + textField.hashCode()
        result = 31 * result + actionButton.hashCode()
        result = 31 * result + (cardNumberTextField?.hashCode() ?: 0)
        result = 31 * result + (pinTextField?.hashCode() ?: 0)
        return result
    }
}

/**
 * Provides default appearance configurations for Gift Card UI elements.
 *
 * This object offers pre-defined styling for components related to gift card
 * interactions, such as the gift card entry screen or gift card display.
 * It includes default settings for spacing, text fields, and action buttons,
 * providing a consistent look and feel.
 */
object GiftCardAppearanceDefaults {

    /**
     * Creates a default appearance configuration for a gift card UI.
     *
     * This function provides a standard look and feel for gift card-related UI elements,
     * specifying spacing, text field appearance, and action button appearance.
     *
     * @return A [GiftCardWidgetAppearance] object configured with default values.
     */
    @Composable
    fun appearance(): GiftCardWidgetAppearance = GiftCardWidgetAppearance(
        verticalSpacing = WidgetDefaults.Spacing,
        horizontalSpacing = WidgetDefaults.Spacing,
        textFieldVerticalSpacing = WidgetDefaults.Spacing,
        textFieldHorizontalSpacing = WidgetDefaults.Spacing,
        textField = TextFieldAppearanceDefaults.appearance().copy(singleLine = true),
        actionButton = ButtonAppearanceDefaults.filledButtonAppearance().copy(
            text = stringResource(R.string.button_submit)
        ),
        // Seed the default placeholder/hint strings into the per-field appearance so they are
        // discoverable and editable via the appearance object (e.g. shown in the styling screen)
        // rather than living only as fallbacks inside each input component.
        cardNumberTextField = TextFieldAppearanceDefaults.appearance().copy(
            singleLine = true,
            placeholderText = stringResource(R.string.placeholder_card_number),
            hintText = stringResource(R.string.hint_gift_card_number)
        ),
        pinTextField = TextFieldAppearanceDefaults.appearance().copy(
            singleLine = true,
            placeholderText = stringResource(R.string.placeholder_card_pin),
            hintText = stringResource(R.string.hint_gift_card_pin)
        )
    )

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

@SdkLightDarkPreviews
@Composable
internal fun PreviewGiftCardDetails() {
    GiftCardWidget(
        config = GiftCardWidgetConfig("accessToken"),
        completion = {}
    )
}