package com.paydock.designsystems.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.extensions.autofill
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

/**
- * Composable function to display a text input field with various customization options.
+ * A composable function that renders a customizable text input field.
+ * This field provides a wide range of options for styling and interaction, including:
+ * - Placeholder and label text
+ * - Enable/disable state
+ * - Read-only mode
+ * - Error message display
+ * - Visual transformations
+ * - Keyboard options and actions
+ * - Single-line or multi-line input
+ * - Autofill support
+ * - Trailing and leading icons
 *
- * @param modifier Modifier for the text input field layout.
- * @param value The current value of the text input field.
- * @param onValueChange Callback function triggered when the value of the text input field changes.
- * @param placeholder Placeholder text displayed when the text input field is empty.
- * @param label Label text displayed above the text input field.
- * @param enabled Flag to determine whether the text input field is enabled for user input.
- * @param readOnly Flag to determine whether the text input field is read-only.
- * @param error Error message displayed below the text input field when there's an input validation error.
- * @param visualTransformation Transformation applied to the visual representation of the text input field.
- * @param keyboardOptions Keyboard configuration options for the text input field.
- * @param keyboardActions Keyboard actions for handling keyboard events.
- * @param singleLine Flag to determine whether the text input field supports single-line input.
- * @param maxLines Maximum number of lines allowed in the text input field.
- * @param autofillType An optional [AutofillType] indicating the type of data that can be
- * autofilled for this input field (e.g., address, postal code). If provided, it enables
- * autofill support.
- * @param interactionSource Interaction source for tracking user interactions with the text input field.
- * @param trailingIcon Icon displayed at the end of the text input field.
- * @param leadingIcon Icon displayed at the start of the text input field.
+ * @param modifier [Modifier] to be */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SdkTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    label: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    autofillType: AutofillType? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    // State to track whether the text input field is focused
    var isFocused by remember { mutableStateOf(false) }
    // State to track whether the label should be displayed based on focus and text input
    val shouldShowLabel = isFocused || value.isNotEmpty()

    // Set the current label text style based on the focused state and whether there's text
    val labelTextStyle = if (shouldShowLabel) {
        Theme.typography.label
    } else {
        Theme.typography.label.copy(fontSize = Theme.typography.body1.fontSize)
    }

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                }
                .testTag("sdkInput")
                .autofillModifier(autofillType, onValueChange),
            enabled = enabled, // Use the parameter value
            readOnly = readOnly,
            textStyle = Theme.typography.body1.copy(color = Theme.colors.onSurface),
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(Theme.colors.primary)
        ) { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled, // Use the parameter value
                singleLine = singleLine,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                isError = error != null,
                label = {
                    TextFieldLabel(label, labelTextStyle, maxLines)
                },
                placeholder = {
                    TextFieldPlaceholder(placeholder, maxLines)
                },
                trailingIcon = {
                    TextFieldTrailingIcon(error, trailingIcon)
                },
                leadingIcon = leadingIcon,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = Theme.colors.primary,
                    unfocusedLabelColor = Theme.colors.onBackground
                ),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled, // Use the parameter value
                        isError = error != null,
                        interactionSource = interactionSource,
                        shape = Theme.textFieldShapes.small,
                        focusedBorderThickness = Theme.dimensions.borderWidth.times(2),
                        unfocusedBorderThickness = Theme.dimensions.borderWidth
                    )
                }
            )
        }

        TextFieldErrorLabel(error)
    }
}

/**
 * Displays a label for a text field.
 *
 * This composable renders a simple text label with specified styling and line limits.
 * It's designed to be used as a descriptive label associated with a text field.
 *
 * @param label The text content of the label.
 * @param labelTextStyle The [TextStyle] to apply to the label's text. This controls
 *                       font, color, size, etc. of the label.
 * @param maxLines The maximum number of lines the label can occupy. If the text
 *                 exceeds this limit, it will be truncated.
 */
@Composable
private fun TextFieldLabel(label: String, labelTextStyle: TextStyle, maxLines: Int) {
    Text(
        modifier = Modifier.testTag("sdkLabel"),
        maxLines = maxLines,
        style = labelTextStyle,
        text = label
    )
}

@Composable
private fun TextFieldPlaceholder(placeholder: String, maxLines: Int) {
    Text(
        modifier = Modifier.testTag("sdkPlaceholder"),
        maxLines = maxLines,
        style = Theme.typography.body1,
        text = placeholder
    )
}

/**
 * Displays a trailing icon in a TextField based on the presence of an error.
 *
 * If an error message is provided, it displays a default error icon.
 * Otherwise, it displays the custom trailing icon provided by the caller, if any.
 *
 * @param error The error message to display. If not null, a default error icon will be shown.
 * @param trailingIcon An optional composable lambda to display as a custom trailing icon.
 *                     If `error` is null and `trailingIcon` is provided, this icon will be displayed.
 *                     If `error` is not null, this icon will be ignored and the error icon will be shown.
 */
@Composable
private fun TextFieldTrailingIcon(error: String?, trailingIcon: @Composable (() -> Unit)?) {
    if (error != null) {
        TextFieldErrorIcon()
    } else {
        trailingIcon?.invoke()
    }
}

/**
 * Displays an error icon, typically used to indicate an error state within a TextField.
 *
 * This composable renders a predefined error icon with the error color from the current theme.
 * It is designed to be used in conjunction with a text field or other input component to visually
 * represent that the input is invalid or an error has occurred.
 *
 * The icon uses a test tag "errorIcon" to be found easily in UI tests.
 *
 * @see androidx.compose.material3.TextField
 * @see androidx.compose.material3.OutlinedTextField
 *
 * Example usage:
 * ```
 * var text by remember { mutableStateOf("") }
 * var isError by remember { mutableStateOf(false) }
 *
 * Column {
 *     OutlinedTextField(
 *         value = text,
 *         onValueChange = {
 *             text = it
 *             isError = it.length < 5 // Example error condition
 *         },
 *         isError = isError,
 *         trailingIcon = {
 *              if(isError){
 *                  TextFieldErrorIcon()
 *              }
 *         },
 *         label = { Text("Enter at least 5 characters") }
 *     )
 * }
 * ```
 */
@Composable
private fun TextFieldErrorIcon() {
    Icon(
        modifier = Modifier.testTag("errorIcon"),
        painter = painterResource(id = R.drawable.ic_error),
        contentDescription = stringResource(id = R.string.content_desc_error_icon),
        tint = Theme.colors.error
    )
}

/**
 * Displays an error label below a TextField if an error message is provided.
 *
 * This composable conditionally renders a Text component to display an error message.
 * It uses [AnimatedVisibility] to smoothly show/hide the error label based on the presence of the error string.
 *
 * @param error The error message string to display. If `null`, the error label will be hidden.
 *              If an empty string is provided it will display an empty label.
 *
 * Example Usage:
 *
 * ```kotlin
 *  TextFieldErrorLabel(error = "This field is required")
 *
 *  TextFieldErrorLabel(error = null) // No error label shown
 *
 *  TextFieldErrorLabel(error = "") // Empty label shown, but visible
 * ```
 */
@Composable
private fun TextFieldErrorLabel(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Text(
            modifier = Modifier
                .padding(start = 15.dp, top = 6.dp)
                .testTag("errorLabel"),
            text = error ?: "",
            color = Theme.colors.error,
            style = Theme.typography.label
        )
    }
}

/**
 * A modifier that enables autofill for a composable element when the provided [autofillType] is not null.
 *
 * This function conditionally applies the [autofill] modifier to a given [Modifier]. If [autofillType] is provided,
 * it will configure the element for autofill with the specified type. If [autofillType] is null, it returns the original
 * modifier without applying any autofill behavior.
 *
 * @param autofillType The type of autofill to enable for the composable element. If null, no autofill is enabled.
 *                     See [AutofillType] for available options.
 * @param onFill A callback that is invoked when autofill is triggered and a value is filled.
 *               It receives the filled string as a parameter.
 * @return The modified [Modifier] with autofill behavior if [autofillType] is not null, otherwise the original [Modifier].
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.autofillModifier(autofillType: AutofillType?, onFill: (String) -> Unit): Modifier {
    return if (autofillType != null) {
        this.autofill(
            autofillTypes = listOf(autofillType),
            onFill = onFill,
        )
    } else {
        this
    }
}

/**
 * Composable function to preview an empty state of the SdkTextField.
 */
@OptIn(ExperimentalComposeUiApi::class)
@PreviewLightDark
@Composable
internal fun PreviewSdkTextFieldEmptyState() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("") }
    // Preview SdkTextField with an empty state
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            SdkTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Placeholder",
                label = "Label",
                error = if (value == "error") "This is error" else null
            )
        }
    }
}

/**
 * Composable function to preview the SdkTextField with input.
 */
@OptIn(ExperimentalComposeUiApi::class)
@PreviewLightDark
@Composable
internal fun PreviewSdkTextFieldWithInput() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("Input") }
    // Preview SdkTextField with pre-filled input
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            SdkTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Placeholder",
                label = "Label"
            )
        }
    }
}

/**
 * Composable function to preview the SdkTextField with an error state.
 */
@OptIn(ExperimentalComposeUiApi::class)
@PreviewLightDark
@Composable
internal fun PreviewSdkTextFieldError() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("xxxx") }
    // Preview SdkTextField with error state
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            SdkTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Placeholder",
                label = "Label",
                error = "Enter valid input",
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = "Error Icon",
                    )
                }
            )
        }
    }
}
