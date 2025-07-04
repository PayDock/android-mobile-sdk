package com.paydock.designsystems.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.paydock.core.presentation.ui.previews.SdkFontScalePreviews
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews

/**
 * A composable function that displays text with customizable appearance settings.
 *
 * This function wraps the standard [Text] composable to provide a convenient way to display
 * text with consistent styling based on a [TextAppearance] object. It allows you to easily
 * control various aspects of text rendering, including style, overflow behavior, line wrapping,
 * and line limits.
 *
 * @param modifier The [Modifier] to be applied to the text.
 * @param text The string of text to display.
 * @param appearance An optional [TextAppearance] object that defines the appearance of the text.
 *                   Defaults to [TextAppearanceDefaults.appearance()] if not provided.
 *                   This object encapsulates properties like style, overflow, softWrap, maxLines, and minLines.
 *
 * @see Text
 * @see TextAppearance
 * @see TextAppearanceDefaults
 */
@Composable
internal fun SdkText(
    modifier: Modifier = Modifier,
    text: String,
    appearance: TextAppearance = TextAppearanceDefaults.appearance()
) {
    Text(
        modifier = modifier,
        text = text,
        style = appearance.style,
        overflow = appearance.overflow,
        softWrap = appearance.softWrap,
        maxLines = appearance.maxLines,
        minLines = appearance.minLines,
    )
}

/**
 * A composable function that displays text with specified styling and behavior, tailored for SDK use.
 *
 * This function is a wrapper around the standard [Text] composable, providing a convenient way to display
 * text with common styling options through the [TextAppearance] parameter. It is designed for internal use
 * within the SDK to ensure consistent text rendering across different components.
 *
 * @param modifier The modifier to be applied to the text.
 * @param text The text to be displayed, represented as an [AnnotatedString]. This allows for rich text formatting.
 * @param appearance The [TextAppearance] that defines the visual style and behavior of the text,
 *                   such as font, color, text overflow, and number of lines.
 *                   Defaults to [TextAppearanceDefaults.appearance()].
 *
 * @see Text
 * @see TextAppearance
 * @see TextAppearanceDefaults
 */
@Composable
internal fun SdkText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    appearance: TextAppearance = TextAppearanceDefaults.appearance()
) {
    Text(
        modifier = modifier,
        text = text,
        style = appearance.style,
        overflow = appearance.overflow,
        softWrap = appearance.softWrap,
        maxLines = appearance.maxLines,
        minLines = appearance.minLines,
    )
}

/**
 * Defines the appearance of text within a layout.
 *
 * This class encapsulates the various styling and layout properties that
 * affect how text is displayed, including its style, overflow behavior,
 * line wrapping, and line limits.
 *
 * @property style The [TextStyle] that defines the font, color, size, and other
 *                 visual attributes of the text.
 * @property overflow The [TextOverflow] behavior to use when text exceeds the
 *                    available space.
 * @property softWrap Whether the text should wrap at soft line breaks.
 *                   If `false`, the text will only wrap at hard line breaks.
 * @property maxLines The maximum number of lines to display. If the text
 *                    exceeds this limit, it will be truncated according to
 *                    the [overflow] property. A value of `Int.MAX_VALUE`
 *                    indicates no limit.
 * @property minLines The minimum number of lines to occupy. If the text does
 *                    not require this many lines to display, it may be
 *                    padded with empty lines. A value of `1` is the minimum.
 */
@Immutable
class TextAppearance(
    val style: TextStyle,
    val overflow: TextOverflow,
    val softWrap: Boolean,
    val maxLines: Int,
    val minLines: Int
) {

    /**
     * Creates a new [TextAppearance] instance with the specified properties,
     * copying values from the current instance if not provided.
     *
     * @param style The text style to apply to the text. Defaults to the style of this [TextAppearance].
     * @param overflow How visual overflow should be handled. Defaults to the overflow of this [TextAppearance].
     * @param softWrap Whether the text should break at soft line breaks. If false, the text will not wrap.
     *                 Defaults to the softWrap setting of this [TextAppearance].
     * @param maxLines The maximum number of lines the text should span, or null for no limit.
     *                 Defaults to the maxLines setting of this [TextAppearance].
     * @param minLines The minimum number of lines the text should span.
     *                 Defaults to the minLines setting of this [TextAppearance].
     * @return A new [TextAppearance] object with the specified or copied properties.
     */
    fun copy(
        style: TextStyle = this.style,
        overflow: TextOverflow = this.overflow,
        softWrap: Boolean = this.softWrap,
        maxLines: Int = this.maxLines,
        minLines: Int = this.minLines
    ): TextAppearance = TextAppearance(
        style = style.copy(),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextAppearance

        if (softWrap != other.softWrap) return false
        if (maxLines != other.maxLines) return false
        if (minLines != other.minLines) return false
        if (style != other.style) return false
        if (overflow != other.overflow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = softWrap.hashCode()
        result = 31 * result + maxLines
        result = 31 * result + minLines
        result = 31 * result + style.hashCode()
        result = 31 * result + overflow.hashCode()
        return result
    }
}

/**
 * `TextAppearanceDefaults` provides a set of default values for configuring a [TextAppearance].
 *
 * This object offers a convenient way to obtain a `TextAppearance` with common default settings,
 * such as inheriting the current text style, clipping overflowing text, allowing soft wrapping,
 * and having no upper limit on the number of lines while defaulting to at least one line.
 */
object TextAppearanceDefaults {

    /**
     * Defines the default text appearance for a composable.
     *
     * This function provides a standard configuration for text display, including:
     * - `style`: The default text style inherited from the current context's `LocalTextStyle`.
     * - `overflow`: `TextOverflow.Clip`, ensuring text that exceeds the available space is clipped.
     * - `softWrap`: `true`, enabling text wrapping to fit within the available space.
     * - `maxLines`: `Int.MAX_VALUE`, allowing the text to span an unlimited number of lines.
     * - `minLines`: `1`, ensuring at least one line of text is displayed, even if the content is empty.
     *
     * This provides a baseline appearance that can be further customized by the caller if needed.
     *
     * @return A `TextAppearance` object representing the default text appearance.
     */
    @Composable
    fun appearance(): TextAppearance = TextAppearance(
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Clip,
        softWrap = true,
        maxLines = Int.MAX_VALUE,
        minLines = 1,
    )
}

@SdkFontScalePreviews
@Composable
internal fun SdkTextDefaultPreview() {
    SdkText(text = "Hello, World!")
}

@SdkLightDarkPreviews
@Composable
internal fun SdkTextAppearancePreview() {
    SdkText(
        text = "Hello, World!",
        appearance = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium.copy(
                Color.Red
            )
        )
    )
}