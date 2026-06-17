@file:Suppress("TooManyFunctions")

package com.paydock.designsystems.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkFontScalePreviews
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.IconAppearance
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults.appearance
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.theme.Success
import com.paydock.feature.card.presentation.components.CardSchemeIcon

/**
 * Number of frames to wait after scrolling the target field into view before moving TalkBack
 * focus to it, so its on-screen position is stable and focus resolves to the correct field
 * (rather than whatever element previously occupied this field's pre-scroll position).
 */
private const val ACCESSIBILITY_FOCUS_SETTLE_FRAMES = 3

/**
 * Number of frames over which the accessibility-focus scroll re-asserts top alignment, to
 * outlast the accessibility framework's own scroll-into-view (~5 frames ≈ 80ms is enough to
 * cover its single requestRectangleOnScreen pass without a noticeable hold).
 */
private const val ACCESSIBILITY_SCROLL_REASSERT_FRAMES = 5

/**
 * When true, [SdkTextField] suppresses its focus-loss error announcement. A form provides this
 * while it programmatically clears focus on submit, so the per-field error announcement doesn't
 * collide with the form-level error-count and first-error-field announcements. Defaults to false,
 * so fields used outside such a form keep announcing errors on defocus as normal.
 */
internal val LocalSuppressFocusLossErrorAnnouncement = compositionLocalOf { false }

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
 * @param label The label text to display above the text field.
 * @param enabled Controls the enabled state of the text field. When false, the text field will
 *   be visually disabled and will not respond to user input.
 * @param showValidIcon Whether to show a validation success icon when the input is not empty
 *   and there's no error.
 * @param placeholder The placeholder text to display when the text field is empty.
 * @param error The error message to display below the text field. If null, no error is shown.
 * @param hint The hint message to display below the text field when no error is present.
 * @param autofillType The type of content for autofill purposes.
 * @param a11yFocus Whether the text field should be focused for accessibility purposes.
 * @param visualTransformation The visual transformation to apply to the text (e.g., for passwords).
 * @param keyboardOptions Options for configuring the software keyboard.
 * @param keyboardActions Actions to be performed when specific keyboard keys are pressed.
 * @param interactionSource The [MutableInteractionSource] to use for tracking interaction events.
 * @param trailingIcon An optional composable to display as a trailing icon. Note that this
 *   might be overridden by error or success icons if they are active.
 * @param leadingIcon An optional composable to display as a leading icon.
 * @param leadingIconDescription Optional accessibility description for the leading icon. When provided,
 *   it is included in the field's content description and announced via TalkBack when it changes
 *   (e.g. when a card scheme is detected for a card number field).
 */
@Composable
internal fun SdkTextField(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = appearance(),
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    enabled: Boolean = true,
    showValidIcon: Boolean = true,
    placeholder: String? = null,
    error: String? = null,
    hint: String? = null,
    autofillType: ContentType? = null,
    a11yFocus: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    leadingIconDescription: String? = null,
) {
    val showHint = !hint.isNullOrEmpty()
    val showPlaceholder = !placeholder.isNullOrEmpty()
    val isRequired = !label.contains("(Optional)", ignoreCase = true)
    val focusRequester = remember { FocusRequester() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val requiredLabel = stringResource(R.string.accessibility_required)
    val disabledLabel = stringResource(id = R.string.accessibility_state_disabled)
    val editingLabel = stringResource(id = R.string.accessibility_state_editing)
    val validLabel = stringResource(id = R.string.content_desc_valid_icon)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val view = LocalView.current
    var announce by remember { mutableStateOf(false) }
    // Read once per composition; the focus-loss effect below re-runs (and re-reads this) whenever
    // focus changes, so it reflects the value at the moment focus is lost.
    val suppressFocusLossAnnouncement = LocalSuppressFocusLossErrorAnnouncement.current

    // When the field gains full (editing) focus, clear any visible error until the user's next
    // change. On defocus the error shows again, and a change to `value` resumes error display.
    var suppressErrorOnFocus by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) { suppressErrorOnFocus = isFocused }
    LaunchedEffect(value) { suppressErrorOnFocus = false }

    val effectiveError = if (suppressErrorOnFocus) null else error
    val isError = effectiveError != null

    // Base the valid icon on the real error so an invalid value never shows a "valid" tick while
    // its error is temporarily suppressed.
    val isShowingValidIcon = error == null && showValidIcon && value.isNotEmpty()

    // Determine if we should render a trailing icon; avoid reserving space when not needed
    val trailingIconComposable: (@Composable (() -> Unit))? = when {
        isError -> ({ TextFieldErrorIcon() })
        isShowingValidIcon -> ({ TextFieldValidIcon(appearance.validIcon) })
        else -> trailingIcon
    }

    // Logic to handle accessibility focus and announcements
    LaunchedEffect(a11yFocus) {
        if (a11yFocus) {
            announce = false // Reset announcement state

            // Scroll the field's TOP into view first. A zero-size rect aligns the top edge so a
            // field taller than the viewport isn't bottom-aligned (which would clip its top at
            // large accessibility sizes). bringIntoView suspends until the scroll animation ends.
            bringIntoViewRequester.bringIntoView(Rect.Zero)

            // Let layout fully settle before moving accessibility focus. TalkBack resolves the
            // focus event against the field's on-screen position, so focusing before the scroll
            // has committed intermittently landed focus on whatever element previously sat at
            // this field's location (e.g. the expiry field). Waiting a few frames makes the
            // target field's position stable so focus reliably lands on it.
            repeat(ACCESSIBILITY_FOCUS_SETTLE_FRAMES) { withFrameNanos { } }

            announce = true // Trigger accessibility (TalkBack) focus + readout

            // Setting `focused = true` makes the framework scroll the focused node using its FULL
            // bounds, bottom-aligning a too-tall field and re-clipping the top. Re-assert the top
            // alignment for a short window so our scroll is the final one; once aligned it's a
            // no-op.
            repeat(ACCESSIBILITY_SCROLL_REASSERT_FRAMES) {
                bringIntoViewRequester.bringIntoView(Rect.Zero)
                withFrameNanos { }
            }
        } else {
            announce = false
        }
    }

    // Announce when the valid icon transitions to showing
    LaunchedEffect(isShowingValidIcon) {
        if (isShowingValidIcon) {
            @Suppress("DEPRECATION")
            view.announceForAccessibility(validLabel)
        }
    }

    // Announce when the leading icon description changes (e.g., card scheme detection).
    LaunchedEffect(leadingIconDescription) {
        leadingIconDescription?.takeIf { it.isNotEmpty() }?.let { description ->
            @Suppress("DEPRECATION")
            view.announceForAccessibility(description)
        }
    }

    // Politely announce a field-scoped error when the field loses focus while invalid, so a
    // TalkBack user hears the problem even though focus has already moved to the next field.
    // Gated on the focus loss transition (true -> false) rather than on error changes while
    // unfocused, so a submit-time validation pass — which forces errors without moving focus —
    // doesn't double up with the form-level error-count live region. The field name is included
    // because focus has already moved on, so a bare "Error: ..." would sound like it belongs to
    // the next field.
    // Tracks the error last announced *in focus* so the focus-loss announcement below can skip
    // re-announcing the identical error the user already heard while typing. Reset each time the
    // field gains focus, so a later editing session that surfaces the same error only on blur
    // still announces it.
    var inFocusAnnouncedError by remember { mutableStateOf<String?>(null) }
    var wasFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        when {
            wasFocused && !isFocused -> {
                // Focus lost: announce the error (named, for orientation) unless it's the same one
                // already announced in-focus while the user was editing, or the focus change was
                // triggered by a form submit (which clears focus and runs its own error-count and
                // first-error-field announcements that would otherwise be drowned out).
                if (error != null && error != inFocusAnnouncedError && !suppressFocusLossAnnouncement) {
                    @Suppress("DEPRECATION")
                    view.announceForAccessibility(
                        listOfNotNull(label.takeIf { it.isNotEmpty() }, "Error: $error").joinToString(", ")
                    )
                }
            }
            !wasFocused && isFocused -> {
                // Fresh focus: reset the in-focus dedupe for this editing session.
                inFocusAnnouncedError = null
            }
        }
        wasFocused = isFocused
    }

    // Politely announce when a focused field flips INTO the error state as the user edits — the
    // false -> true transition only, not every keystroke — mirroring iOS, which re-reads the error
    // from the field's hint during typing. No field name here: focus is still on this field, so the
    // context is unambiguous (matching iOS's bare "Error: ..." read). This complements the
    // focus-loss announcement above, which covers errors that only surface on blur (e.g. an
    // invalid-length card number whose error is suppressed while the field is focused).
    var wasInError by remember { mutableStateOf(false) }
    LaunchedEffect(effectiveError, isFocused) {
        val nowInError = effectiveError != null
        if (isFocused && nowInError && !wasInError) {
            @Suppress("DEPRECATION")
            view.announceForAccessibility("Error: $error")
            // Record it so the focus-loss announcement doesn't repeat the same error on blur.
            inFocusAnnouncedError = error
        }
        wasInError = nowInError
    }

    Column(
        modifier = modifier
            .testTag("sdkInput")
            .bringIntoViewRequester(bringIntoViewRequester)
            .focusRequester(focusRequester)
            .clearAndSetSemantics {
                // Configure accessibility semantics for the entire text field column. Reflect both
                // the actual editing focus (isFocused) and the programmatic a11y focus (announce),
                // so TalkBack knows the field is currently focused while the user is editing it.
                focused = isFocused || announce

                // Build a natural language content description for screen readers
                val contentParts = mutableListOf(label)

                if (value.isNotEmpty()) {
                    // Add spaces between digits so accessibility doesn't announce as one big number
                    contentParts.add(value.map { "$it " }.joinToString("").trim())
                }

                contentParts.add("Edit box")

                if (isError) {
                    // Mirror iOS: in the error state keep the readout focused on what to fix —
                    // suppress the contextual extras (scheme, "Valid", example, hint, custom hint,
                    // "required") and append the error LAST. Placing it at the end of the content
                    // description — rather than in stateDescription, which TalkBack reads early
                    // alongside states like "disabled" — matches both iOS and the native Android
                    // convention of announcing the error after the field's label and value.
                    contentParts.add("Error: $error")
                } else {
                    leadingIconDescription?.takeIf { it.isNotEmpty() }
                        ?.let { contentParts.add(it) }

                    if (isShowingValidIcon) {
                        contentParts.add(validLabel)
                    }

                    // Placeholder example is always announced (when empty + focused), regardless of
                    // any custom hint.
                    if (value.isEmpty() && isFocused && showPlaceholder) {
                        contentParts.add("Example: $placeholder")
                    }

                    // A custom hintDescription overrides ONLY the hint text; otherwise fall back to
                    // the visual hint. The label, value, scheme, valid state, placeholder and
                    // "required" suffix are all still announced. e.g. an integrator can replace the
                    // hint "Format MM/YY" with "Enter your card expiry date in format MM slash YY".
                    val customHint = appearance.hintDescription?.takeIf { it.isNotEmpty() }
                    if (customHint != null) {
                        contentParts.add(customHint)
                    } else if (showHint) {
                        contentParts.add(hint)
                    }

                    if (isRequired) {
                        contentParts.add(requiredLabel)
                    }
                }

                contentDescription = contentParts.joinToString(", ")

                // State description carries non-error states (disabled, editing). The error is
                // folded into the content description above so TalkBack announces it last.
                val stateParts = mutableListOf<String>()
                if (!enabled) {
                    stateParts.add(disabledLabel)
                }
                if (isFocused) {
                    // Spoken indication that the field is currently focused/being edited.
                    stateParts.add(editingLabel)
                }
                if (stateParts.isNotEmpty()) {
                    stateDescription = stateParts.joinToString(", ")
                }

                // Tells accessibility readout this content type
                autofillType?.let { contentType = it }

                // Only offer the "activate to edit" click action when the field isn't already
                // focused — re-announcing "double tap to activate" on a field that's currently
                // being edited is misleading.
                if (!isFocused) {
                    onClick(label = appearance.clickableDescription) {
                        focusRequester.requestFocus()
                        true
                    }
                }
            },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled,
            textStyle = appearance.style,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            label = {
                // Use appearance.label directly to preserve custom styling in both focused and unfocused states
                // Previously, unfocused state was overriding with MaterialTheme.typography.labelMedium,
                // which prevented custom styling from being applied to the collapsed label
                TextFieldLabel(label, appearance.label)
            },
            placeholder = {
                if (showPlaceholder) {
                    TextFieldPlaceholder(placeholder, appearance.placeholder)
                }
            },
            trailingIcon = trailingIconComposable,
            leadingIcon = leadingIcon,
            singleLine = appearance.singleLine,
            colors = appearance.colors,
            shape = appearance.shape
        )
        if (isError) {
            TextFieldErrorLabel(error, appearance)
        } else if (showHint) {
            TextFieldHintLabel(hint, appearance)
        }
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
 * @property hintLabel The [TextAppearance] for the hint label displayed below the text field when an error has not occurred.
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
    val hintLabel: TextAppearance,
    val validIcon: IconAppearance,
    // DecorationBox
    val singleLine: Boolean,
    val colors: TextFieldColors,
    val shape: Shape,
    // Padding
    val topMessageSpacing: Dp,
    val startMessageSpacing: Dp,
    // Content
    val placeholderText: String?,
    val hintText: String?,
    // Accessibility
    val hintDescription: String?,
    val clickableDescription: String?
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
     * @param label The appearance of the hint label. Defaults to the label of the current [TextFieldAppearance].
     * @param validIcon The appearance of the valid icon. Defaults to the valid icon of the current [TextFieldAppearance].
     * @param singleLine Whether the text field should be single-line or multi-line.
     *      Defaults to the singleLine value of the current [TextFieldAppearance].
     * @param colors The colors used for different parts of the text field. Defaults to the colors of the current [TextFieldAppearance].
     * @param shape The shape of the text field's background. Defaults to the shape of the current [TextFieldAppearance].
     * @param topMessageSpacing Top spacing between error or hint messages and TextField.
     * @param startMessageSpacing Left spacing between error or hint messages and TextField.
     * @param placeholderText Override default placeholder text for field.
     * @param hintText Override default hint text for field.
     * @param hintDescription A custom accessibility readout that replaces only the hint text of the
     *  field's content description. The field's label, current value, card scheme, valid state,
     *  placeholder example and "required" suffix are still announced. For example, replace the hint
     *  "Format MM/YY" with "Enter your card expiry date in format MM slash YY".
     * @param clickableDescription Override default field accessibility readouts for clickable description.
     * @return A new [TextFieldAppearance] instance with the specified properties.
     */
    fun copy(
        // BasicTextField
        style: TextStyle = this.style,
        placeholder: TextAppearance = this.placeholder,
        label: TextAppearance = this.label,
        error: TextAppearance = this.errorLabel,
        hintLabel: TextAppearance = this.hintLabel,
        validIcon: IconAppearance = this.validIcon,
        // DecorationBox
        singleLine: Boolean = this.singleLine,
        colors: TextFieldColors = this.colors,
        shape: Shape = this.shape,
        // Padding
        topMessageSpacing: Dp = this.topMessageSpacing,
        startMessageSpacing: Dp = this.startMessageSpacing,
        // Content
        placeholderText: String? = this.placeholderText,
        hintText: String? = this.hintText,
        // Accessibility
        hintDescription: String? = this.hintDescription,
        clickableDescription: String? = this.clickableDescription
    ): TextFieldAppearance = TextFieldAppearance(
        // BasicTextField
        style = style.copy(),
        placeholder = placeholder.copy(),
        label = label.copy(),
        errorLabel = error.copy(),
        hintLabel = hintLabel.copy(),
        validIcon = validIcon.copy(),
        // DecorationBox
        singleLine = singleLine,
        colors = colors.copy(),
        shape = shape,
        // Padding
        topMessageSpacing = topMessageSpacing,
        startMessageSpacing = startMessageSpacing,
        // Content
        placeholderText = placeholderText,
        hintText = hintText,
        // Accessibility
        hintDescription = hintDescription,
        clickableDescription = clickableDescription
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
        if (hintLabel != other.hintLabel) return false
        if (validIcon != other.validIcon) return false
        if (colors != other.colors) return false
        if (shape != other.shape) return false
        if (topMessageSpacing != other.topMessageSpacing) return false
        if (startMessageSpacing != other.startMessageSpacing) return false
        if (placeholderText != other.placeholderText) return false
        if (hintText != other.hintText) return false
        if (hintDescription != other.hintDescription) return false
        if (clickableDescription != other.clickableDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = singleLine.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + placeholder.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + errorLabel.hashCode()
        result = 31 * result + hintLabel.hashCode()
        result = 31 * result + validIcon.hashCode()
        result = 31 * result + colors.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + topMessageSpacing.hashCode()
        result = 31 * result + startMessageSpacing.hashCode()
        result = 31 * result + placeholderText.hashCode()
        result = 31 * result + hintText.hashCode()
        result = 31 * result + hintDescription.hashCode()
        result = 31 * result + clickableDescription.hashCode()
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
        // BasicTextField - Keep natural for container expansion
        style = MaterialTheme.typography.bodyMedium,
        // DecorationBox
        singleLine = false,
        placeholder = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        ),
        // Label - Apply alignment fixes for icon alignment
        label = TextAppearanceDefaults.appearance().copy(
            maxLines = 2,
            style = MaterialTheme.typography.labelMedium
        ),
        errorLabel = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error)
        ),
        hintLabel = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2
        ),
        validIcon = IconAppearanceDefaults.appearance().copy(tint = Success),
        colors = OutlinedTextFieldDefaults.colors(),
        shape = OutlinedTextFieldDefaults.shape,
        topMessageSpacing = 6.dp,
        startMessageSpacing = 0.dp,
        placeholderText = null,
        hintText = null,
        hintDescription = null,
        clickableDescription = null
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
    appearance: TextAppearance = appearance().label,
) {
    SdkText(
        modifier = Modifier
            .testTag("sdkLabel")
            .clearAndSetSemantics { },
        text = label,
        appearance = appearance
    )
}

/**
 * Preview for the text field label.
 */
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
    appearance: TextAppearance = appearance().placeholder
) {
    SdkText(
        modifier = Modifier
            .testTag("sdkPlaceholder")
            .clearAndSetSemantics { },
        text = placeholder,
        appearance = appearance
    )
}

/**
 * Preview for the text field placeholder.
 */
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
private fun TextFieldValidIcon(appearance: IconAppearance = appearance().validIcon) {
    SdkIcon(
        modifier = Modifier
            .testTag("successIcon")
            .clearAndSetSemantics { },
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
 * @see OutlinedTextField
 */
@Composable
private fun TextFieldErrorIcon() {
    SdkIcon(
        modifier = Modifier
            .testTag("errorIcon")
            .clearAndSetSemantics { },
        painter = painterResource(id = R.drawable.ic_error),
        contentDescription = null,
        appearance = IconAppearanceDefaults.appearance()
            .copy(tint = MaterialTheme.colorScheme.error)
    )
}

/**
 * Preview for the text field error icon.
 */
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
    appearance: TextFieldAppearance = appearance()
) {
    AnimatedVisibility(visible = error != null) {
        SdkText(
            modifier = Modifier
                .padding(
                    start = appearance.startMessageSpacing,
                    top = appearance.topMessageSpacing
                )
                .testTag("errorLabel")
                .clearAndSetSemantics { },
            text = error ?: "",
            appearance = appearance.errorLabel
        )
    }
}

/**
 * Preview for the text field error label.
 */
@SdkFontScalePreviews
@Composable
internal fun TextFieldErrorLabelPreview() {
    Column {
        TextFieldErrorLabel(error = "This is an error message")
    }
}

/**
 * Displays a hint label below a text field when no error is present and a hint is provided.
 *
 * @param hint The hint message to display.
 * @param appearance The [TextAppearance] to use for styling the hint label.
 */
@Composable
private fun TextFieldHintLabel(
    hint: String,
    appearance: TextFieldAppearance = appearance()
) {
    val hintAppearance = appearance.copy(
        style = appearance.style.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    )
    SdkText(
        modifier = Modifier
            .padding(
                start = appearance.startMessageSpacing,
                top = appearance.topMessageSpacing
            )
            .testTag("hintLabel")
            .clearAndSetSemantics { },
        text = hint,
        appearance = appearance.hintLabel
    )
}

/**
 * Preview for the text field hint label.
 */
@SdkFontScalePreviews
@Composable
internal fun TextFieldHintLabelPreview() {
    Column {
        TextFieldHintLabel(hint = "This is a hint message")
    }
}

/**
 * Composable function to preview an empty state of the SdkTextField.
 */
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
        error = if (value == "error") "This is error" else null,
        hint = "Preview hint"
    )
}

/**
 * Preview for the SdkTextField in an empty state with a leading icon.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextFieldWithLeadingIconEmptyState() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("") }
    // Preview SdkTextField with pre-filled input
    SdkTextField(
        value = value,
        leadingIcon = { CardSchemeIcon(null, false) },
        onValueChange = { value = it },
        placeholder = "Placeholder",
        label = "Label",
        hint = "Preview Hint"
    )
}

/**
 * Preview for the SdkTextField with input and a leading icon.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextFieldWithLeadingIconWithInput() {
    // Mutable state for the text input field value
    var value by remember { mutableStateOf("Input") }
    // Preview SdkTextField with pre-filled input
    SdkTextField(
        value = value,
        leadingIcon = { CardSchemeIcon(null, false) },
        onValueChange = { value = it },
        placeholder = "Placeholder",
        label = "Label",
        hint = "Preview Hint"
    )
}

/**
 * Composable function to preview the SdkTextField with input.
 */
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
        label = "Label",
        hint = "Preview Hint"
    )
}

/**
 * Composable function to preview the SdkTextField with an error state.
 */
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
        error = "Enter valid input",
        hint = "Preview Hint"
    )
}
