package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.ui.CardScheme

/**
 * A composable function that renders input fields for cardholder and payment details.
 *
 * This function provides input fields for:
 * - Cardholder's name (optional)
 * - Card number
 * - Expiry date
 * - Security code
 *
 * It supports custom configurations, focus management, and dynamic UI adjustments based on font scaling.
 *
 * @param shouldCollectCardholderName Determines whether the cardholder's name field is displayed.
 * @param schemeConfig Defines the supported card schemes and validation settings.
 * @param verticalSpacing The spacing between the expiry and security code input fields
 * when laid out in a column. Defaults to [WidgetDefaults.Spacing].
 * @param horizontalSpacing The spacing between the expiry and security code input fields
 * when laid out in a row. Defaults to [WidgetDefaults.Spacing].
 * @param textFieldAppearance The appearance configuration for the text fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param focusCardNumber A [FocusRequester] to manage focus for the card number input.
 * @param focusExpiry A [FocusRequester] to manage focus for the expiry date input.
 * @param focusCode A [FocusRequester] to manage focus for the security code input.
 * @param enabled Controls whether all input fields are enabled or disabled.
 * @param cardHolderName The current text value of the cardholder name input field.
 * @param cardNumber The current text value of the card number input field.
 * @param expiry The current text value of the expiry date input field.
 * @param code The current text value of the security code input field.
 * @param cardScheme The detected [CardScheme] based on the card number input.
 * @param onCardHolderNameChange A callback triggered when the cardholder name input changes.
 * @param onCardNumberChange A callback triggered when the card number input changes.
 * @param onExpiryChange A callback triggered when the expiry date input changes.
 * @param onSecurityCodeChange A callback triggered when the security code input changes.
 *
 * UI Behavior:
 * - If `shouldCollectCardholderName` is `true`, the cardholder name input field is shown.
 * - When the system's font scale exceeds a predefined threshold ([MobileSDKConstants.CardDetailsConfig.FONT_SCALE_THRESHOLD]),
 */
@Suppress("LongParameterList")
@Composable
internal fun CardInputFields(
    shouldCollectCardholderName: Boolean,
    schemeConfig: SupportedSchemeConfig,
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    horizontalSpacing: Dp = WidgetDefaults.Spacing,
    textFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    focusCardNumber: FocusRequester,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    enabled: Boolean,
    cardHolderName: String,
    cardNumber: String,
    expiry: String,
    code: String,
    cardScheme: CardScheme?,
    onCardHolderNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Define threshold for large font scale
    val largeFontScaleThreshold = MobileSDKConstants.CardDetailsConfig.FONT_SCALE_THRESHOLD

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        // Cardholder Name Input
        if (shouldCollectCardholderName) {
            CardHolderNameInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cardHolderInput"),
                appearance = textFieldAppearance,
                value = cardHolderName,
                enabled = enabled,
                nextFocus = focusCardNumber,
                onValueChange = onCardHolderNameChange
            )
        }

        // Card Number Input
        CreditCardNumberInput(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusCardNumber)
                .testTag("cardNumberInput"),
            appearance = textFieldAppearance,
            schemeConfig = schemeConfig,
            value = cardNumber,
            cardScheme = cardScheme,
            enabled = enabled,
            onValueChange = onCardNumberChange,
            nextFocus = focusExpiry
        )

        // Expiry and Security Code Inputs
        if (fontScale >= largeFontScaleThreshold) {
            ExpiryAndCodeColumn(
                verticalSpacing = verticalSpacing,
                appearance = textFieldAppearance,
                expiry = expiry,
                code = code,
                focusExpiry = focusExpiry,
                focusCode = focusCode,
                enabled = enabled,
                cardScheme = cardScheme,
                onExpiryChange = onExpiryChange,
                onSecurityCodeChange = onSecurityCodeChange
            )
        } else {
            ExpiryAndCodeRow(
                horizontalSpacing = horizontalSpacing,
                appearance = textFieldAppearance,
                expiry = expiry,
                code = code,
                focusExpiry = focusExpiry,
                focusCode = focusCode,
                enabled = enabled,
                cardScheme = cardScheme,
                onExpiryChange = onExpiryChange,
                onSecurityCodeChange = onSecurityCodeChange
            )
        }
    }
}

/**
 * Composable function to display the expiry and security code input fields in a column layout.
 *
 * @param verticalSpacing The spacing between the expiry and security code input fields. Defaults to [WidgetDefaults.Spacing].
 * @param appearance The appearance configuration for the text fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param expiry The expiry date value to be displayed in the input field.
 * @param code The security code value to be displayed in the input field.
 * @param focusExpiry The [FocusRequester] used to manage focus for the expiry date input.
 * @param focusCode The [FocusRequester] used to manage focus for the security code input.
 * @param enabled A flag indicating whether the input fields should be enabled or disabled.
 * @param cardScheme The detected [CardScheme] based on the card number input.
 * @param onExpiryChange Callback function to handle changes to the expiry date input value.
 * @param onSecurityCodeChange Callback function to handle changes to the security code input value.
 */
@Composable
private fun ExpiryAndCodeColumn(
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    expiry: String,
    code: String,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    enabled: Boolean,
    cardScheme: CardScheme?,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.Start
    ) {
        CardExpiryInput(
            modifier = Modifier
                .focusRequester(focusExpiry)
                .testTag("cardExpiryInput"),
            appearance = appearance,
            value = expiry,
            enabled = enabled,
            nextFocus = focusCode,
            onValueChange = onExpiryChange,
        )
        CardSecurityCodeInput(
            modifier = Modifier
                .focusRequester(focusCode)
                .testTag("cardSecurityCodeInput"),
            appearance = appearance,
            value = code,
            enabled = enabled,
            cardCode = cardScheme?.code,
            nextFocus = null,
            onValueChange = onSecurityCodeChange
        )
    }
}

/**
 * Composable function to display the expiry and security code input fields in a row layout.
 *
 * @param horizontalSpacing The spacing between the expiry and security code input fields. Defaults to [WidgetDefaults.Spacing].
 * @param appearance The appearance configuration for the text fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param expiry The expiry date value to be displayed in the input field.
 * @param code The security code value to be displayed in the input field.
 * @param focusExpiry The [FocusRequester] used to manage focus for the expiry date input.
 * @param focusCode The [FocusRequester] used to manage focus for the security code input.
 * @param enabled A flag indicating whether the input fields should be enabled or disabled.
 * @param cardScheme The detected [CardScheme] based on the card number input. Used to determine
 * the appropriate security code label and format.
 * @param onExpiryChange Callback function to handle changes to the expiry date input value.
 * @param onSecurityCodeChange Callback function to handle changes to the security code input value.
 */
@Composable
private fun ExpiryAndCodeRow(
    horizontalSpacing: Dp = WidgetDefaults.Spacing,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    expiry: String,
    code: String,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    enabled: Boolean,
    cardScheme: CardScheme?,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Top
    ) {
        CardExpiryInput(
            modifier = Modifier
                .weight(0.5f)
                .focusRequester(focusExpiry)
                .testTag("cardExpiryInput"),
            appearance = appearance,
            value = expiry,
            enabled = enabled,
            onValueChange = onExpiryChange,
            nextFocus = focusCode
        )
        CardSecurityCodeInput(
            modifier = Modifier
                .weight(0.5f)
                .focusRequester(focusCode)
                .testTag("cardSecurityCodeInput"),
            value = code,
            enabled = enabled,
            cardCode = cardScheme?.code,
            nextFocus = null,
            onValueChange = onSecurityCodeChange
        )
    }
}