package com.paydock.designsystems.components.link

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkFontScalePreviews
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable function that renders a clickable text link.
 *
 * This function displays text with a primary color style by default, resembling a hyperlink.
 * The text appearance can be customized using the [appearance] parameter.
 * When clicked, it triggers the provided [onClick] callback.
 *
 * @param modifier The [Modifier] to be applied to the underlying composable.
 * @param linkText The text content of the link.
 * @param appearance The [LinkTextAppearance] to apply to the text. By default, it uses the default link text appearance.
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
        // Render the link text with accessibility semantics and click handling
        SdkText(
            modifier = modifier
                .sizeIn(minHeight = 24.dp) // Ensure minimum touch target height
                .wrapContentSize(Alignment.Center)
                .clickable {
                    onClick()
                }
                .semantics {
                    // Custom click label for accessibility
                    onClick(label = appearance.clickableDescription) {
                        onClick()
                        true
                    }
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
 * @property clickableDescription The description to use for accessibility readout.
 */
@Immutable
class LinkTextAppearance(
    val textAppearance: TextAppearance,
    val clickableDescription: String?
) {
    /**
     * Creates a copy of this [LinkTextAppearance] with optionally modified parameters.
     *
     * @param textAppearance The new [TextAppearance] to use for the copy. Defaults to the current [textAppearance].
     * @param clickableDescription The description to use for accessibility readout.
     * @return A new [LinkTextAppearance] instance with the specified parameters.
     */
    fun copy(
        textAppearance: TextAppearance = this.textAppearance,
        clickableDescription: String? = this.clickableDescription
    ) = LinkTextAppearance(
        textAppearance = textAppearance,
        clickableDescription = clickableDescription
    )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinkTextAppearance

        if (textAppearance != other.textAppearance) return false
        if (clickableDescription != other.clickableDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textAppearance.hashCode()
        result = 31 * result + (clickableDescription?.hashCode() ?: 0)
        return result
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
        ),
        clickableDescription = stringResource(id = R.string.accessibility_click_open_browser)
    )
}

/**
 * Provides a preview for the [SdkLinkText] composable.
 */
@SdkFontScalePreviews
@Composable
internal fun PreviewLinkText() {
    SdkLinkText(linkText = "Privacy Policy") { }
}