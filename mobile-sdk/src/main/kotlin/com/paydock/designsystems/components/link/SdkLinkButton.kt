package com.paydock.designsystems.components.link

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.SdkTextButton
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable function that renders a clickable text link styled as a button.
 *
 * This function displays text that is clickable and visually resembles a link.
 * The appearance of the link button can be customized using the [appearance] parameter.
 * When clicked, it triggers the provided [onClick] callback.
 *
 * @param modifier The [Modifier] to be applied to the underlying composable.
 * @param linkText The text content of the link button.
 * @param appearance The appearance configuration for the link button. Defaults to [LinkButtonAppearanceDefaults.appearance].
 * @param onClick A callback function that is invoked when the link button is clicked.
 */
@Composable
internal fun SdkLinkButton(
    modifier: Modifier = Modifier,
    linkText: String,
    appearance: LinkButtonAppearance = LinkButtonAppearanceDefaults.appearance(),
    onClick: () -> Unit
) {
    Box {
        SdkTextButton(
            modifier = modifier.wrapContentSize(Alignment.Center),
            text = linkText,
            appearance = appearance.actionButton
        ) {
            onClick()
        }
    }
}

/**
 * Represents the visual appearance of a [SdkLinkButton].
 *
 * This class encapsulates the [ButtonAppearance.TextButtonAppearance] specifically used for a link button,
 * allowing customization of properties like height, padding, and text appearance.
 *
 * @property actionButton The underlying [ButtonAppearance.TextButtonAppearance] for the link button.
 */
@Immutable
class LinkButtonAppearance(
    val actionButton: ButtonAppearance.TextButtonAppearance
) {
    /**
     * Creates a copy of the current [LinkButtonAppearance] with optionally overridden button appearance.
     *
     * @param actionButton The [ButtonAppearance.TextButtonAppearance] to use for the copied instance.
     *                         Defaults to the current button appearance.
     * @return A new [LinkButtonAppearance] instance with the specified or default button appearance.
     */
    fun copy(
        actionButton: ButtonAppearance.TextButtonAppearance = this.actionButton
    ) = LinkButtonAppearance(actionButton = actionButton.copy())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinkButtonAppearance

        return actionButton == other.actionButton
    }

    override fun hashCode(): Int {
        return actionButton.hashCode()
    }
}

/**
 * Default values and configurations for [LinkTextAppearance].
 *
 * This object provides a default [LinkTextAppearance] that can be used for [SdkLinkText].
 */
object LinkButtonAppearanceDefaults {

    private val LinkButtonHeight = 24.dp

    /**
     * Provides a default [LinkTextAppearance] for links.
     *
     * This appearance applies a primary color and a slightly larger font size to the text,
     * resembling a typical hyperlink style within the theme.
     *
     * @return The default [LinkTextAppearance].
     */
    @Composable
    fun appearance() = LinkButtonAppearance(
        actionButton = ButtonAppearanceDefaults.textButtonAppearance().copy(
            height = LinkButtonHeight,
            contentPadding = PaddingValues(0.dp),
            textAppearance = TextAppearanceDefaults.appearance().copy(
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value + 1).sp

                )
            )
        )
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewLinkButton() {
    SdkLinkButton(linkText = "Privacy Policy") { }
}