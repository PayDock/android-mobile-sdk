package com.paydock.feature.card.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.RenderButton
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
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
 * @param config The configuration for the gift card widget, including necessary access details.
 * @param appearance Defines the visual appearance of the gift card widget elements. Defaults to a standard appearance.
 * @param loadingDelegate An optional delegate to manage the visibility of loading indicators externally.
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
    completion: (Result<String>) -> Unit,
) {
    // ViewModel instance scoped to the Koin dependency injection framework
    val viewModel: GiftCardViewModel = koinViewModel(parameters = { parametersOf(config) })

    // Observing state flows for input and UI state
    val inputState by viewModel.inputStateFlow.collectAsState()
    val uiState by viewModel.stateFlow.collectAsState()

    val isEnabled by remember(uiState) {
        derivedStateOf { inputState.isDataValid && uiState !is GiftCardUIState.Loading && enabled }
    }
    val isLoading by remember(uiState) { derivedStateOf { loadingDelegate == null && uiState is GiftCardUIState.Loading } }

    // Focus handlers for input fields
    val focusCardNumber = FocusRequester()
    val focusCardPin = FocusRequester()

    // React to changes in the UI state
    LaunchedEffect(uiState) {
        handleUIState(uiState, viewModel, loadingDelegate, completion)
    }

    // Composing the UI
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appearance.verticalSpacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                appearance.horizontalSpacing,
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
                appearance = appearance.textField,
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
                appearance = appearance.textField,
                value = inputState.pin,
                enabled = uiState !is GiftCardUIState.Loading && enabled,
                onValueChange = { viewModel.updateCardPin(it) }
            )
        }

        // Submit button
        appearance.actionButton.RenderButton(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("addCard"),
            text = stringResource(R.string.button_submit),
            enabled = isEnabled,
            isLoading = isLoading,
        ) {
            viewModel.tokeniseCard()
        }
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
    val textField: TextFieldAppearance,
    val actionButton: ButtonAppearance,
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
        textField: TextFieldAppearance = this.textField,
        actionButton: ButtonAppearance = this.actionButton,
    ): GiftCardWidgetAppearance = GiftCardWidgetAppearance(
        verticalSpacing = verticalSpacing.takeOrElse { this.verticalSpacing },
        horizontalSpacing = horizontalSpacing.takeOrElse { this.horizontalSpacing },
        textField = textField.copy(),
        actionButton = when (actionButton) {
            is ButtonAppearance.FilledButtonAppearance -> actionButton.copy()
            is ButtonAppearance.IconButtonAppearance -> actionButton.copy()
            is ButtonAppearance.OutlineButtonAppearance -> actionButton.copy()
            is ButtonAppearance.TextButtonAppearance -> actionButton.copy()
        },
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiftCardWidgetAppearance

        if (verticalSpacing != other.verticalSpacing) return false
        if (horizontalSpacing != other.horizontalSpacing) return false
        if (textField != other.textField) return false
        if (actionButton != other.actionButton) return false

        return true
    }

    override fun hashCode(): Int {
        var result = verticalSpacing.hashCode()
        result = 31 * result + horizontalSpacing.hashCode()
        result = 31 * result + textField.hashCode()
        result = 31 * result + actionButton.hashCode()
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
        textField = TextFieldAppearanceDefaults.appearance().copy(singleLine = true),
        actionButton = ButtonAppearanceDefaults.filledButtonAppearance()
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
    GiftCardWidget(config = GiftCardWidgetConfig("accessToken")) {

    }
}