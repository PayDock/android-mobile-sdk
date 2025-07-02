@file:Suppress("TooManyFunctions")

package com.paydock.designsystems.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.extensions.autofill
import com.paydock.core.presentation.extensions.defaultErrorSemantics
import com.paydock.core.presentation.ui.previews.SdkFontScalePreviews
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.IconAppearance
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.theme.Success

/**
 * A customizable text field component that provides various styling and functionality options.
 *
 * This composable wraps the Material Design [BasicTextField] and [OutlinedTextFieldDefaults.DecorationBox]
 * to offer a tailored text field experience. It allows setting a placeholder, label, error state,
 * custom icons, and more.
 *
 * @param modifier Modifier to be applied to the text field.
 * @param appearance Customization options for the text field's appearance, such as colors, shape, and text styles.
 *                   Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param value The current text value of the text field.
 * @param onValueChange Callback that is triggered when the text value changes. Provides the new text value.
 * @param placeholder The placeholder text to display when the text field is empty.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SdkTextField(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    label: String = "",
    enabled: Boolean = true,
    showValidIcon: Boolean = true,
    error: String? = null,
    autofillType: AutofillType? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    // State to track whether the text input field is focused
    var isFocused by remember { mutableStateOf(false) }
    val isError = error != null

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                }
                .testTag("sdkInput")
                .autofillModifier(autofillType, onValueChange)
                .defaultErrorSemantics(isError, error ?: stringResource(R.string.error_input_default)),
            enabled = enabled,
            textStyle = appearance.style,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            label = {
                // State to track whether the label should be displayed based on focus and text input
                val shouldShowLabel = isFocused || value.isNotEmpty()

                // Set the current label text style based on the focused state and whether there's text
                val labelTextAppearance = if (shouldShowLabel) {
                    appearance.label
                } else {
                    appearance.label.copy(
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    )
                }
                TextFieldLabel(label, labelTextAppearance)
            },
            placeholder = {
                TextFieldPlaceholder(placeholder, appearance.placeholder)
            },
            trailingIcon = {
                when {
                    isError -> TextFieldErrorIcon()
                    showValidIcon && value.isNotEmpty() -> TextFieldValidIcon(appearance.validIcon)
                    else -> trailingIcon?.invoke()
                }
            },
            leadingIcon = leadingIcon,
            isError = isError,
            colors = appearance.colors,
            shape = appearance.shape,
        )
        TextFieldErrorLabel(error, appearance.errorLabel)
    }
}

/**
 * Defines the visual appearance of a text field.
 *
 * This class encapsulates various styling properties for both the [BasicTextField]
 * and the surrounding decoration box of a text field in Jetpack Compose. It includes
 * settings for text, placeholder, labels, icons, colors, padding, and borders.
 *
 * @property style The [TextStyle] to apply to the input text within the text field.
 * @property placeholder The [TextAppearance] for the placeholder text displayed when the text field is empty.
 * @property label The [TextAppearance] for the label displayed above the text field.
 * @property errorLabel The [TextAppearance] for the error label displayed below the text field when an error occurs.
 * @property validIcon The [IconAppearance] for the icon displayed when the input is valid.
 * @property singleLine Whether the text field should be limited to a single line of text.
 * @property colors The [TextFieldColors] to use for the different states of the text field (e.g., focused, unfocused, disabled).
 * @property shape The [Shape] to use for the background of the text field.
 */
@Immutable
class TextFieldAppearance(
    // BasicTextField
    val style: TextStyle,
    val placeholder: TextAppearance,
    val label: TextAppearance,
    val errorLabel: TextAppearance,
    val validIcon: IconAppearance,
    // DecorationBox
    val singleLine: Boolean,
    val colors: TextFieldColors,
    val shape: Shape
) {
    /**
     * Creates a copy of this [TextFieldAppearance], optionally overriding some of the values.
     *
     * This function allows creating a new [TextFieldAppearance] instance based on the current one,
     * but with modifications to specific properties. It's useful for creating variations of a
     * text field's appearance without modifying the original.
     *
     * @param style The text style of the input text. Defaults to the style of the current [TextFieldAppearance].
     * @param placeholder The appearance of the placeholder text. Defaults to the placeholder of the current [TextFieldAppearance].
     * @param label The appearance of the label text. Defaults to the label of the current [TextFieldAppearance].
     * @param error The appearance of the error label text. Defaults to the error label of the current [TextFieldAppearance].
     * @param validIcon The appearance of the valid icon. Defaults to the valid icon of the current [TextFieldAppearance].
     * @param singleLine Whether the text field should be single-line or multi-line.
     *      Defaults to the singleLine value of the current [TextFieldAppearance].
     * @param colors The colors used for different parts of the text field. Defaults to the colors of the current [TextFieldAppearance].
     * @param shape The shape of the text field's background. Defaults to the shape of the current [TextFieldAppearance].
     * @return A new [TextFieldAppearance] instance with the specified properties.
     */
    fun copy(
        // BasicTextField
        style: TextStyle = this.style,
        placeholder: TextAppearance = this.placeholder,
        label: TextAppearance = this.label,
        error: TextAppearance = this.errorLabel,
        validIcon: IconAppearance = this.validIcon,
        // DecorationBox
        singleLine: Boolean = this.singleLine,
        colors: TextFieldColors = this.colors,
        shape: Shape = this.shape,
    ): TextFieldAppearance = TextFieldAppearance(
        // BasicTextField
        style = style.copy(),
        placeholder = placeholder.copy(),
        label = label.copy(),
        errorLabel = error.copy(),
        validIcon = validIcon.copy(),
        // DecorationBox
        singleLine = singleLine,
        colors = colors.copy(),
        shape = shape,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextFieldAppearance

        if (singleLine != other.singleLine) return false
        if (style != other.style) return false
        if (placeholder != other.placeholder) return false
        if (label != other.label) return false
        if (errorLabel != other.errorLabel) return false
        if (validIcon != other.validIcon) return false
        if (colors != other.colors) return false
        if (shape != other.shape) return false

        return true
    }

    override fun hashCode(): Int {
        var result = singleLine.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + placeholder.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + errorLabel.hashCode()
        result = 31 * result + validIcon.hashCode()
        result = 31 * result + colors.hashCode()
        result = 31 * result + shape.hashCode()
        return result
    }
}

/**
 * `TextFieldAppearanceDefaults` provides default appearance configurations for text fields.
 *
 * It offers a set of pre-configured styles and visual elements that can be used to quickly
 * set up the appearance of `TextField` components in your application. These defaults align
 * with the Material Design 3 guidelines and can be easily customized or overridden as needed.
 *
 * The main entry point is the [appearance] function, which returns a [TextFieldAppearance] object
 * encapsulating all the default settings.
 */
object TextFieldAppearanceDefaults {

    /**
     * Defines the default appearance of a text field, including styling, colors, and other visual properties.
     *
     * This function provides a pre-configured [TextFieldAppearance] instance suitable for use with
     * Material Design 3 guidelines. It includes settings for:
     *
     * - **Basic Text Field Styling:**
     *   - `style`: Sets the text style using `MaterialTheme.typography.bodyMedium`.
     *   - `cursorBrush`: Sets the cursor color using `MaterialTheme.colorScheme.primary`.
     *
     * - **Decoration Box Styling:**
     *   - `singleLine`: Set to `false` allowing for multi-line text input.
     *   - `placeholder`: Configures the appearance of the placeholder text, inheriting from [TextAppearanceDefaults]
     *     but overriding the text style to `MaterialTheme.typography.bodyMedium` and restricting to a single line.
     *   - `label`: Configures the appearance of the label text, inheriting from [TextAppearanceDefaults],
     *     restricting to a single line, and using `MaterialTheme.typography.labelMedium`.
     *   - `errorLabel`: Configures the appearance of the error label text, inheriting from [TextAppearanceDefaults],
     *     using `MaterialTheme.typography.labelMedium` with the color set to `MaterialTheme.colorScheme.error`.
     *   - `validIcon`: Configures the appearance of the valid icon, using [IconAppearanceDefaults] and
     *    setting its tint to the Success color.
     * - **Outlined Text Field Styling:**
     *   - `colors`: Uses the default colors provided by `OutlinedTextFieldDefaults.colors()`.
     *   - `contentPadding`: Uses the default content padding provided by `OutlinedTextFieldDefaults.contentPadding()`.
     */
    @Composable
    fun appearance(): TextFieldAppearance = TextFieldAppearance(
        // BasicTextField
        style = MaterialTheme.typography.bodyMedium,
        // DecorationBox
        singleLine = false,
        placeholder = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        ),
        label = TextAppearanceDefaults.appearance().copy(
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium
        ),
        errorLabel = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error)
        ),
        validIcon = IconAppearanceDefaults.appearance().copy(tint = Success),
        colors = OutlinedTextFieldDefaults.colors(),
        shape = OutlinedTextFieldDefaults.shape,
    )
}

/**
 * A composable function that displays a label for a text field.
 *
 * This function renders a label using the provided [label] string and [appearance].
 * It's designed to be used alongside text fields to provide context and descriptions.
 *
 * @param label The text content of the label. This will be displayed as the label text.
 * @param appearance The styling configuration for the label text. Defaults to the label appearance
 *   defined in [TextFieldAppearanceDefaults]. This allows customization of font, color, etc.
 */
@Composable
private fun TextFieldLabel(
    label: String,
    appearance: TextAppearance = TextFieldAppearanceDefaults.appearance().label,
) {
    SdkText(
        modifier = Modifier.testTag("sdkLabel"),
        text = label,
        appearance = appearance
    )
}

@SdkFontScalePreviews
@Composable
internal fun TextFieldLabelPreview() {
    Column {
        TextFieldLabel(label = "This is a label")
    }
}

/**
 * A composable function that displays a placeholder text for a text field.
 *
 * This function utilizes the [SdkText] composable to render the placeholder
 * string with the specified appearance. It's primarily intended to provide
 * visual cues to the user about what kind of input is expected in an empty
 * text field.
 *
 * @param placeholder The string to be displayed as the placeholder text.
 * @param appearance The [TextAppearance] to apply to the placeholder text.
 *                   Defaults to [TextFieldAppearanceDefaults.appearance().placeholder].
 */
@Composable
private fun TextFieldPlaceholder(
    placeholder: String,
    appearance: TextAppearance = TextFieldAppearanceDefaults.appearance().placeholder
) {
    SdkText(
        modifier = Modifier.testTag("sdkPlaceholder"),
        text = placeholder,
        appearance = appearance
    )
}

@SdkFontScalePreviews
@Composable
internal fun TextFieldPlaceholderPreview() {
    Column {
        TextFieldPlaceholder(placeholder = "This is a placeholder label")
    }
}

/**
 * Composable function that displays a valid/success icon within a text field.
 *
 * This function renders a checkmark icon, typically used to visually indicate
 * that the content of a text field has passed validation or meets certain criteria.
 *
 * @param appearance  An [IconAppearance] object that defines the visual styling of the icon.
 *                    Defaults to [TextFieldAppearanceDefaults.appearance().validIcon] if not specified.
 *
 * @see IconAppearance
 * @see TextFieldAppearanceDefaults
 * @see SdkIcon
 */
@SdkLightDarkPreviews
@Composable
private fun TextFieldValidIcon(appearance: IconAppearance = TextFieldAppearanceDefaults.appearance().validIcon) {
    SdkIcon(
        modifier = Modifier.testTag("successIcon"),
        painter = painterResource(id = R.drawable.ic_success),
        contentDescription = stringResource(id = R.string.content_desc_valid_icon),
        appearance = appearance
    )
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
 */
@Composable
private fun TextFieldErrorIcon() {
    SdkIcon(
        modifier = Modifier.testTag("errorIcon"),
        painter = painterResource(id = R.drawable.ic_error),
        contentDescription = null,
        appearance = IconAppearanceDefaults.appearance()
            .copy(tint = MaterialTheme.colorScheme.error)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun TextFieldErrorIconPreview() {
    Column {
        TextFieldErrorIcon()
    }
}

/**
 * Displays an error label below a text field when an error is present.
 *
 * This composable conditionally renders an error message using [SdkText].
 * The visibility of the error label is controlled by the [error] parameter.
 * If [error] is null, the label is hidden; otherwise, the label is displayed
 * with the provided error message.
 *
 * @param error The error message to display. If null, the error label will be hidden.
 * @param appearance The [TextAppearance] to use for styling the error label. Defaults to
 *                   [TextFieldAppearanceDefaults.appearance().errorLabel].
 */
@Composable
private fun TextFieldErrorLabel(
    error: String?,
    appearance: TextAppearance = TextFieldAppearanceDefaults.appearance().errorLabel
) {
    AnimatedVisibility(visible = error != null) {
        SdkText(
            modifier = Modifier
                .padding(start = 15.dp, top = 6.dp)
//                .semantics { this.invisibleToUser() }
                .testTag("errorLabel"),
            text = error ?: "",
            appearance = appearance
        )
    }
}

@SdkFontScalePreviews
@Composable
internal fun TextFieldErrorLabelPreview() {
    Column {
        TextFieldErrorLabel(error = "This is an error message")
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
@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextFieldEmptyState() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("") }
    // Preview SdkTextField with an empty state
    SdkTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "Placeholder",
        label = "Label",
        error = if (value == "error") "This is error" else null
    )
}

/**
 * Composable function to preview the SdkTextField with input.
 */
@OptIn(ExperimentalComposeUiApi::class)
@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextFieldWithInput() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("Input") }
    // Preview SdkTextField with pre-filled input
    SdkTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "Placeholder",
        label = "Label"
    )
}

/**
 * Composable function to preview the SdkTextField with an error state.
 */
@OptIn(ExperimentalComposeUiApi::class)
@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextFieldError() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("xxxx") }
    // Preview SdkTextField with error state
    SdkTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "Placeholder",
        label = "Label",
        error = "Enter valid input"
    )
}
