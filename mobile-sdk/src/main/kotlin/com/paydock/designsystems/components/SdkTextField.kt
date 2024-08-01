package com.paydock.designsystems.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

/**
 * Composable function to display a text input field with various customization options.
 *
 * @param modifier Modifier for the text input field layout.
 * @param value The current value of the text input field.
 * @param onValueChange Callback function triggered when the value of the text input field changes.
 * @param placeholder Placeholder text displayed when the text input field is empty.
 * @param label Label text displayed above the text input field.
 * @param enabled Flag to determine whether the text input field is enabled for user input.
 * @param readOnly Flag to determine whether the text input field is read-only.
 * @param error Error message displayed below the text input field when there's an input validation error.
 * @param visualTransformation Transformation applied to the visual representation of the text input field.
 * @param keyboardOptions Keyboard configuration options for the text input field.
 * @param keyboardActions Keyboard actions for handling keyboard events.
 * @param singleLine Flag to determine whether the text input field supports single-line input.
 * @param maxLines Maximum number of lines allowed in the text input field.
 * @param interactionSource Interaction source for tracking user interactions with the text input field.
 * @param trailingIcon Icon displayed at the end of the text input field.
 * @param leadingIcon Icon displayed at the start of the text input field.
 */
@Suppress("LongMethod")
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    // State to track whether the text input field is focused
    var focusedState by remember { mutableStateOf(false) }
    // State to track whether the label should be displayed based on focus and text input
    val focusedAndHasText = focusedState || value.isNotEmpty()
    // Set the current label text style based on the focused state and whether there's text
    val labelTextStyle = if (focusedAndHasText) {
        Theme.typography.label
    } else {
        Theme.typography.label.copy(fontSize = Theme.typography.body1.fontSize)
    }

    // Column composable to contain the text input field and error label
    Column(modifier = modifier) {
        // OutlinedTextField composable for the text input field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    focusedState = it.isFocused
                }
                .testTag("sdkInput"),
            enabled = enabled,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            maxLines = maxLines,
            interactionSource = interactionSource,
            trailingIcon = if (error != null) {
                // Display error icon if there's an error message
                {
                    Icon(
                        modifier = Modifier.testTag("errorIcon"),
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = stringResource(id = R.string.content_desc_error_icon),
                        tint = Theme.colors.error
                    )
                }
            } else {
                trailingIcon
            },
            leadingIcon = leadingIcon,
            textStyle = Theme.typography.body1,
            label = {
                // Display label text
                Text(
                    modifier = Modifier.testTag("sdkLabel"),
                    maxLines = maxLines,
                    style = labelTextStyle,
                    text = label,
                    color = Theme.colors.onBackground
                )
            },
            placeholder = {
                // Display placeholder text
                Text(
                    modifier = Modifier.testTag("sdkPlaceholder"),
                    maxLines = maxLines,
                    style = Theme.typography.body1,
                    text = placeholder,
                    color = Theme.colors.onBackground
                )
            },
            isError = error != null,
            shape = Theme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                // Error Colors
                errorLabelColor = Theme.colors.onSurfaceVariant,
                errorBorderColor = Theme.colors.outline,
                errorCursorColor = Theme.colors.primary,
            ),
        )

        // Animated error label visibility
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
}

/**
 * Composable function to preview an empty state of the SdkTextField.
 */
@LightDarkPreview
@Composable
private fun PreviewSdkTextFieldEmptyState() {
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
@LightDarkPreview
@Composable
private fun PreviewSdkTextFieldWithInput() {
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
@LightDarkPreview
@Composable
private fun PreviewSdkTextFieldError() {
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
