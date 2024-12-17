package com.paydock.designsystems.components.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon

/**
 * Composable function to display a customizable SDK button with various button types.
 *
 * This function provides flexibility in creating buttons with different styles, shapes, icons, and loading states.
 * It is designed to adapt to various UI needs while maintaining a consistent design system.
 *
 * @param modifier Modifier to be applied to the button's layout for customization.
 * @param text The text displayed on the button, describing its action or purpose.
 * @param enabled Determines whether the button is interactive. When false, the button is visually and functionally disabled.
 * @param type Specifies the visual style of the button. Defaults to `AppButtonType.Filled`,
 * but can also be `Outlined` or `Text` for alternate styles.
 * @param isLoading Indicates if the button should display a loading state, disabling user interaction while showing a loader.
 * @param buttonIcon An optional icon to display on the button, defined as a `ButtonIcon`. Supports vector (`ImageVector`)
 * or drawable (`@DrawableRes`) resources for flexibility in icon customization.
 * @param buttonColor The background color of the button. Defaults to the primary theme color.
 * @param buttonShape Defines the shape of the button (e.g., rounded corners). Defaults to the small shape defined in the theme.
 * @param onClick A callback function invoked when the button is clicked. This will be disabled if `isLoading` is true.
 */
@Composable
internal fun SdkButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    type: AppButtonType = AppButtonType.Filled,
    isLoading: Boolean = false,
    buttonIcon: ButtonIcon? = null,
    buttonColor: Color = Theme.colors.primary,
    buttonShape: Shape = Theme.buttonShapes.small,
    onClick: () -> Unit,
) {
    // Decide button appearance based on type
    when (type) {
        AppButtonType.Filled -> PrimaryButton(
            modifier = modifier.height(Theme.dimensions.buttonHeight),
            onClick = onClick,
            enabled = enabled && !isLoading,
            shape = buttonShape,
            content = {
                ButtonContent(
                    text = text,
                    buttonIcon = buttonIcon,
                    isLoading = isLoading
                )
            }
        )

        AppButtonType.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(Theme.dimensions.buttonHeight),
            enabled = enabled && !isLoading,
            content = {
                ButtonContent(
                    text = text,
                    buttonIcon = buttonIcon,
                    isLoading = isLoading
                )
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = buttonColor,
                containerColor = Color.Transparent,
                disabledContainerColor = buttonColor.alpha40,
                disabledContentColor = Theme.colors.onPrimary
            ),
            shape = buttonShape,
            border = BorderStroke(1.dp, buttonColor)
        )

        AppButtonType.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !isLoading,
                shape = buttonShape,
                colors = ButtonDefaults.textButtonColors(
                    disabledContainerColor = buttonColor.alpha40,
                    disabledContentColor = buttonColor.alpha40,
                ),
                content = {
                    ButtonContent(text = text, isLoading = isLoading)
                }
            )
        }
    }
}

/**
 * Composable function to display the primary button.
 *
 * @param modifier Modifier for the button layout.
 * @param onClick Callback function for button click events.
 * @param enabled Flag to determine if the button is enabled.
 * @param shape Shape of the button.
 * @param content Content of the button.
 */
@Composable
private fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    shape: Shape,
    buttonColor: Color = Theme.colors.primary,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
        colors = ButtonDefaults.buttonColors(
            contentColor = Theme.colors.onPrimary,
            disabledContainerColor = buttonColor.alpha40,
            disabledContentColor = Theme.colors.onPrimary,
        ),
        shape = shape
    )
}

/**
 * Composable function to display the content of the button.
 *
 * @param text Text displayed on the button.
 * @param buttonIcon Icon to display on the button, defined as a `ButtonIcon`. This allows for custom vector
 * or drawable resources.
 * @param isLoading Flag to determine if the button is in a loading state.
 */
@Composable
private fun RowScope.ButtonContent(
    text: String,
    buttonIcon: ButtonIcon? = null,
    isLoading: Boolean
) {
    Crossfade(targetState = isLoading, label = "buttonCrossFade") { loadingState ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                Theme.dimensions.buttonSpacing,
                Alignment.CenterHorizontally
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loadingState) {
                SdkButtonLoader()
            } else {
                // Display the icon (if any) and text in a consistent layout
                when (buttonIcon) {
                    is ButtonIcon.Vector -> Icon(
                        modifier = Modifier.size(Theme.dimensions.buttonIconSize),
                        imageVector = buttonIcon.icon,
                        contentDescription = stringResource(id = R.string.content_desc_button_icon),
                    )
                    is ButtonIcon.DrawableRes -> Icon(
                        modifier = Modifier.size(Theme.dimensions.buttonIconSize),
                        painter = painterResource(buttonIcon.drawable),
                        contentDescription = stringResource(id = R.string.content_desc_button_icon),
                    )

                    else -> Unit
                }
            }
            Text(
                text = text,
                modifier = Modifier.align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                style = Theme.typography.button.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

@Composable
@LightDarkPreview
private fun PreviewButtonPrimary() {
    SdkTheme {
        SdkButton(
            text = "Primary",
            onClick = {},
            type = AppButtonType.Filled,
        )
    }
}

@Composable
@LightDarkPreview
private fun PreviewOutlineButton() {
    SdkTheme {
        SdkButton(
            text = "Primary",
            onClick = {},
            type = AppButtonType.Outlined,
        )
    }
}

@Composable
@LightDarkPreview
private fun PreviewTextButton() {
    SdkTheme {
        SdkButton(
            text = "Primary",
            onClick = {},
            type = AppButtonType.Text,
        )
    }
}
