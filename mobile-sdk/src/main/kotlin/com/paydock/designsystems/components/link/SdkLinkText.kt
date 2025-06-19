package com.paydock.designsystems.components.link

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.core.presentation.ui.previews.SdkFontScalePreviews
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable function that renders a clickable text link.
 *
 * This function displays text with an underlined style by default, resembling a hyperlink.
 * The text appearance can be customized using the [appearance] parameter.
 * When clicked, it triggers the provided [onClick] callback.
 *
 * @param modifier The [Modifier] to be applied to the underlying [Text] composable.
 * @param linkText The text content of the link.
 * @param appearance The [TextAppearance] to apply to the text. By default, it uses the theme's default text appearance.
 * @param onClick A callback function that is invoked when the link is clicked.
 */
@Composable
internal fun SdkLinkText(
    modifier: Modifier = Modifier,
    linkText: String,
    appearance: LinkTextAppearance = LinkTextAppearanceDefaults.appearance(),
    onClick: () -> Unit
) {
    Box {
        SdkText(
            modifier = modifier
                .sizeIn(minHeight = 24.dp)
                .wrapContentSize(Alignment.Center)
                .clickable {
                    onClick()
                },
            text = linkText,
            appearance = appearance.textAppearance
        )
    }
}

/**
 * Represents the visual appearance of a link text.
 *
 * This class encapsulates the [TextAppearance] that should be applied to the text of a link.
 *
 * @property textAppearance The [TextAppearance] to be used for the link text.
 */
@Immutable
class LinkTextAppearance(
    val textAppearance: TextAppearance
) {
    /**
     * Creates a copy of this [LinkTextAppearance] with optionally modified parameters.
     *
     * @param textAppearance The new [TextAppearance] to use for the copy. Defaults to the current [textAppearance].
     * @return A new [LinkTextAppearance] instance with the specified parameters.
     */
    fun copy(textAppearance: TextAppearance = this.textAppearance) = LinkTextAppearance(textAppearance)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinkTextAppearance

        return textAppearance == other.textAppearance
    }

    override fun hashCode(): Int {
        return textAppearance.hashCode()
    }
}

/**
 * Default values and configurations for [LinkTextAppearance].
 *
 * This object provides a default [LinkTextAppearance] that can be used for [SdkLinkText].
 */
object LinkTextAppearanceDefaults {
    /**
     * Provides a default [LinkTextAppearance] for links.
     *
     * This appearance applies a primary color and a slightly larger font size to the text,
     * resembling a typical hyperlink style within the theme.
     *
     * @return The default [LinkTextAppearance].
     */
    @Composable
    fun appearance() = LinkTextAppearance(
        textAppearance = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value + 1).sp
            )
        )
    )
}

@SdkFontScalePreviews
@Composable
internal fun PreviewLinkText() {
    SdkLinkText(linkText = "Privacy Policy") { }
}