package com.paydock.designsystems.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.extensions.alpha40
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.designsystems.type.AppButtonType

/**
 * Composable function to display a customizable SDK button with various button types.
 *
 * @param modifier Modifier for the button layout.
 * @param text Text displayed on the button.
 * @param iconVector Image vector icon displayed alongside the button text.
 * @param iconDrawable Drawable resource ID for the icon displayed alongside the button text.
 * @param enabled Flag to determine if the button is enabled.
 * @param isLoading Flag to determine if the button is in a loading state.
 * @param buttonShape Shape of the button.
 * @param type Type of the button (filled, outlined, or text).
 * @param onClick Callback function for button click events.
 */
@Composable
internal fun SdkButton(
    modifier: Modifier = Modifier,
    text: String,
    iconVector: ImageVector? = null,
    @DrawableRes iconDrawable: Int? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonShape: Shape = Theme.shapes.small,
    type: AppButtonType = AppButtonType.Filled,
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
                    vector = iconVector,
                    drawableRes = iconDrawable,
                    isLoading = isLoading
                )
            }
        )

        AppButtonType.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(Theme.dimensions.buttonHeight),
            enabled = enabled && !isLoading,
            content = { ButtonContent(text = text, isLoading = isLoading) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Theme.colors.primary,
                containerColor = Color.Transparent,
                disabledContainerColor = Theme.colors.primary.alpha40,
                disabledContentColor = Theme.colors.onPrimary
            ),
            shape = buttonShape,
            border = BorderStroke(1.dp, Theme.colors.primary)
        )

        AppButtonType.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !isLoading,
                shape = buttonShape,
                colors = ButtonDefaults.textButtonColors(
                    disabledContainerColor = Theme.colors.primary.alpha40,
                    disabledContentColor = Theme.colors.primary.alpha40,
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
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Theme.colors.primary.alpha40,
            disabledContentColor = Theme.colors.onPrimary,
        ),
        shape = shape
    )
}

/**
 * Composable function to display the content of the button.
 *
 * @param text Text displayed on the button.
 * @param vector Image vector icon displayed alongside the button text.
 * @param drawableRes Drawable resource ID for the icon displayed alongside the button text.
 * @param isLoading Flag to determine if the button is in a loading state.
 */
@Composable
private fun RowScope.ButtonContent(
    text: String,
    vector: ImageVector? = null,
    @DrawableRes drawableRes: Int? = null,
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
            // Display the icon (if any) and text in a consistent layout
            if (vector != null) {
                Icon(
                    modifier = Modifier.size(Theme.dimensions.buttonIconSize),
                    imageVector = vector,
                    contentDescription = stringResource(id = R.string.content_desc_button_icon),
                    tint = Theme.colors.onPrimary
                )
                Spacer(modifier = Modifier.width(Theme.dimensions.buttonSpacing))
            } else if (drawableRes != null) {
                Icon(
                    modifier = Modifier.size(Theme.dimensions.buttonIconSize),
                    painter = painterResource(drawableRes),
                    contentDescription = stringResource(id = R.string.content_desc_button_icon),
                    tint = Theme.colors.onPrimary
                )
                Spacer(modifier = Modifier.width(Theme.dimensions.buttonSpacing))
            }
            Text(
                text = text,
                modifier = Modifier.align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                style = Theme.typography.button.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            if (loadingState) {
                SdkButtonLoader()
            }
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
