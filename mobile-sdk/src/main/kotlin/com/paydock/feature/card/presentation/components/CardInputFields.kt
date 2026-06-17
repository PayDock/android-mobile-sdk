package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.dp
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
 * @param cardNameTextFieldAppearance The appearance configuration for cardholder name field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param cardNumberTextFieldAppearance The appearance configuration for card number name field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param expiryTextFieldAppearance The appearance configuration for expiry field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param securityCodeTextFieldAppearance The appearance configuration for security code field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param focusCardNumber A [FocusRequester] to manage focus for the card number input.
 * @param focusExpiry A [FocusRequester] to manage focus for the expiry date input.
 * @param focusCode A [FocusRequester] to manage focus for the security code input.
 * @param a11yCardNameFocus A [Boolean] used to manage accessibility for the cardholder name input.
 * @param a11yCardNumberFocus A [Boolean] used to manage accessibility for the card number input.
 * @param a11yExpiryFocus A [Boolean] used to manage accessibility for the expiry date input.
 * @param a11ySecurityCodeFocus A [Boolean] used to manage accessibility for the security code input.
 * @param enabled Controls whether all input fields are enabled or disabled.
 * @param cardHolderName The current text value of the cardholder name input field.
 * @param cardNumber The current text value of the card number input field.
 * @param expiry The current text value of the expiry date input field.
 * @param code The current text value of the security code input field.
 * @param cardScheme The detected [CardScheme] based on the card number input.
 * @param cardholderNameErrorOverwrite A [Boolean] to force showing errors for the cardholder name field.
 * @param cardNumberErrorOverwrite A [Boolean] to force showing errors for the card number field.
 * @param cardExpiryErrorOverwrite A [Boolean] to force showing errors for the expiry date field.
 * @param cardSecurityErrorOverwrite A [Boolean] to force showing errors for the security code field.
 * @param onCardHolderNameChange A callback triggered when the cardholder name input changes.
 * @param onCardNumberChange A callback triggered when the card number input changes.
 * @param onExpiryChange A callback triggered when the expiry date input changes.
 * @param onSecurityCodeChange A callback triggered when the security code input changes.
 *
 * UI Behavior:
 * - If `shouldCollectCardholderName` is `true`, the cardholder name input field is shown.
 * - The layout adapts dynamically: when the effective width (considering font scale) is insufficient
 *   for side-by-side fields, they are stacked vertically to prevent clipping.
 */
@Suppress("LongParameterList")
@Composable
internal fun CardInputFields(
    shouldCollectCardholderName: Boolean,
    schemeConfig: SupportedSchemeConfig,
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    horizontalSpacing: Dp = WidgetDefaults.Spacing,
    cardNameTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    cardNumberTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    expiryTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    securityCodeTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    focusCardNumber: FocusRequester,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    a11yCardNameFocus: Boolean = false,
    a11yCardNumberFocus: Boolean = false,
    a11yExpiryFocus: Boolean = false,
    a11ySecurityCodeFocus: Boolean = false,
    enabled: Boolean,
    cardHolderName: String,
    cardNumber: String,
    expiry: String,
    code: String,
    cardScheme: CardScheme?,
    cardholderNameErrorOverwrite: Boolean = false,
    cardNumberErrorOverwrite: Boolean = false,
    cardExpiryErrorOverwrite: Boolean = false,
    cardSecurityErrorOverwrite: Boolean = false,
    onCardHolderNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

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
                appearance = cardNameTextFieldAppearance,
                value = cardHolderName,
                enabled = enabled,
                forceShowErrors = cardholderNameErrorOverwrite,
                nextFocus = focusCardNumber,
                a11yFocus = a11yCardNameFocus,
                onValueChange = onCardHolderNameChange
            )
        }

        // Card Number Input
        CreditCardNumberInput(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusCardNumber)
                .testTag("cardNumberInput"),
            appearance = cardNumberTextFieldAppearance,
            schemeConfig = schemeConfig,
            value = cardNumber,
            cardScheme = cardScheme,
            enabled = enabled,
            forceShowErrors = cardNumberErrorOverwrite,
            nextFocus = focusExpiry,
            a11yFocus = a11yCardNumberFocus,
            onValueChange = onCardNumberChange
        )

        // Expiry and Security Code Inputs - use intelligent layout decision
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Determine if fields should be stacked based on available width and font scaling.
            // For Card Details: Expiry (50% weight) + CVV (50% weight) + spacing
            // Each field needs ~150dp minimum (MM/YY format + CVV digits + padding)
            val shouldUseColumnLayout = WidgetDefaults.shouldUseColumnLayout(
                availableWidth = this.maxWidth,
                fontScale = fontScale,
                minFieldWidth = 150.dp, // Expiry/CVV field minimum
                fieldWeight = 0.5f // Each field weight (equal split)
            )

            if (shouldUseColumnLayout) {
                // Stack fields vertically if width is insufficient
                ExpiryAndCodeColumn(
                    verticalSpacing = verticalSpacing,
                    expiryTextFieldAppearance = expiryTextFieldAppearance,
                    securityCodeTextFieldAppearance = securityCodeTextFieldAppearance,
                    expiry = expiry,
                    code = code,
                    focusExpiry = focusExpiry,
                    focusCode = focusCode,
                    a11yFocusExpiry = a11yExpiryFocus,
                    a11yFocusSecurityCode = a11ySecurityCodeFocus,
                    enabled = enabled,
                    cardScheme = cardScheme,
                    cardExpiryErrorOverwrite = cardExpiryErrorOverwrite,
                    cardSecurityErrorOverwrite = cardSecurityErrorOverwrite,
                    onExpiryChange = onExpiryChange,
                    onSecurityCodeChange = onSecurityCodeChange
                )
            } else {
                // Place fields side-by-side if width allows
                ExpiryAndCodeRow(
                    horizontalSpacing = horizontalSpacing,
                    expiryTextFieldAppearance = expiryTextFieldAppearance,
                    securityCodeTextFieldAppearance = securityCodeTextFieldAppearance,
                    expiry = expiry,
                    code = code,
                    focusExpiry = focusExpiry,
                    focusCode = focusCode,
                    a11yFocusExpiry = a11yExpiryFocus,
                    a11yFocusSecurityCode = a11ySecurityCodeFocus,
                    enabled = enabled,
                    cardScheme = cardScheme,
                    cardExpiryErrorOverwrite = cardExpiryErrorOverwrite,
                    cardSecurityErrorOverwrite = cardSecurityErrorOverwrite,
                    onExpiryChange = onExpiryChange,
                    onSecurityCodeChange = onSecurityCodeChange
                )
            }
        }
    }
}

/**
 * Composable function to display the expiry and security code input fields in a column layout.
 *
 * @param verticalSpacing The spacing between the expiry and security code input fields. Defaults to [WidgetDefaults.Spacing].
 * @param expiryTextFieldAppearance The appearance configuration for expiry field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param securityCodeTextFieldAppearance The appearance configuration for security code field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param expiry The expiry date value to be displayed in the input field.
 * @param code The security code value to be displayed in the input field.
 * @param focusExpiry The [FocusRequester] used to manage focus for the expiry date input.
 * @param focusCode The [FocusRequester] used to manage focus for the security code input.
 * @param a11yFocusExpiry A [Boolean] used to manage accessibility for the expiry date input.
 * @param a11yFocusSecurityCode A [Boolean] used to manage accessibility for the security code input.
 * @param enabled A flag indicating whether the input fields should be enabled or disabled.
 * @param cardScheme The detected [CardScheme] based on the card number input.
 * @param cardExpiryErrorOverwrite A [Boolean] to force showing errors for the expiry date field.
 * @param cardSecurityErrorOverwrite A [Boolean] to force showing errors for the security code field.
 * @param onExpiryChange Callback function to handle changes to the expiry date input value.
 * @param onSecurityCodeChange Callback function to handle changes to the security code input value.
 */
@Composable
private fun ExpiryAndCodeColumn(
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    expiryTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    securityCodeTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    expiry: String,
    code: String,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    a11yFocusExpiry: Boolean,
    a11yFocusSecurityCode: Boolean,
    enabled: Boolean,
    cardScheme: CardScheme?,
    cardExpiryErrorOverwrite: Boolean = false,
    cardSecurityErrorOverwrite: Boolean = false,
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
            appearance = expiryTextFieldAppearance,
            value = expiry,
            enabled = enabled,
            forceShowErrors = cardExpiryErrorOverwrite,
            nextFocus = focusCode,
            a11yFocus = a11yFocusExpiry,
            onValueChange = onExpiryChange
        )
        CardSecurityCodeInput(
            modifier = Modifier
                .focusRequester(focusCode)
                .testTag("cardSecurityCodeInput"),
            appearance = securityCodeTextFieldAppearance,
            value = code,
            enabled = enabled,
            forceShowErrors = cardSecurityErrorOverwrite,
            cardCode = cardScheme?.code,
            nextFocus = null,
            a11yFocus = a11yFocusSecurityCode,
            onValueChange = onSecurityCodeChange
        )
    }
}

/**
 * Composable function to display the expiry and security code input fields in a row layout.
 *
 * @param horizontalSpacing The spacing between the expiry and security code input fields. Defaults to [WidgetDefaults.Spacing].
 * @param expiryTextFieldAppearance The appearance configuration for expiry field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param securityCodeTextFieldAppearance The appearance configuration for security code field. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param expiry The expiry date value to be displayed in the input field.
 * @param code The security code value to be displayed in the input field.
 * @param focusExpiry The [FocusRequester] used to manage focus for the expiry date input.
 * @param focusCode The [FocusRequester] used to manage focus for the security code input.
 * @param a11yFocusExpiry A [Boolean] used to manage accessibility for the expiry date input.
 * @param a11yFocusSecurityCode A [Boolean] used to manage accessibility for the security code input.
 * @param enabled A flag indicating whether the input fields should be enabled or disabled.
 * @param cardScheme The detected [CardScheme] based on the card number input. Used to determine
 * the appropriate security code label and format.
 * @param cardExpiryErrorOverwrite A [Boolean] to force showing errors for the expiry date field.
 * @param cardSecurityErrorOverwrite A [Boolean] to force showing errors for the security code field.
 * @param onExpiryChange Callback function to handle changes to the expiry date input value.
 * @param onSecurityCodeChange Callback function to handle changes to the security code input value.
 */
@Composable
private fun ExpiryAndCodeRow(
    horizontalSpacing: Dp = WidgetDefaults.Spacing,
    expiryTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    securityCodeTextFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    expiry: String,
    code: String,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    a11yFocusExpiry: Boolean,
    a11yFocusSecurityCode: Boolean,
    enabled: Boolean,
    cardScheme: CardScheme?,
    cardExpiryErrorOverwrite: Boolean = false,
    cardSecurityErrorOverwrite: Boolean = false,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit
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
            appearance = expiryTextFieldAppearance,
            value = expiry,
            enabled = enabled,
            forceShowErrors = cardExpiryErrorOverwrite,
            nextFocus = focusCode,
            a11yFocus = a11yFocusExpiry,
            onValueChange = onExpiryChange
        )
        CardSecurityCodeInput(
            modifier = Modifier
                .weight(0.5f)
                .focusRequester(focusCode)
                .testTag("cardSecurityCodeInput"),
            appearance = securityCodeTextFieldAppearance,
            value = code,
            enabled = enabled,
            forceShowErrors = cardSecurityErrorOverwrite,
            cardCode = cardScheme?.code,
            nextFocus = null,
            a11yFocus = a11yFocusSecurityCode,
            onValueChange = onSecurityCodeChange
        )
    }
}