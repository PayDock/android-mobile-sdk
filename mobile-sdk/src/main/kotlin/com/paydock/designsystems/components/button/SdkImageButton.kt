package com.paydock.designsystems.components.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.designsystems.theme.SdkTheme

/**
 * Composable function for creating a custom image button.
 *
 * This button displays an image and handles click events.
 *
 * @param modifier The Modifier to be applied to the button.
 * @param enabled Whether the button is enabled or not. When disabled, the button's appearance changes
 *   (alpha is reduced) and it does not respond to clicks.
 * @param shape The shape of the button.
 * @param rippleColor The color of the ripple effect when the button is pressed.
 * @param painter The [Painter] that will draw the image for the button.
 * @param contentDescription A description of the image for accessibility purposes.
 * @param imageAspectRatio The aspect ratio of the image. If null, the aspect ratio is derived from the painter's intrinsic size.
 *                         This is applied only if the `modifier` specifies a width but not a height.
 * @param onClick The lambda function to be executed when the button is clicked.
 */
@Composable
internal fun SdkImageButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    rippleColor: Color = Color.Transparent,
    painter: Painter,
    contentDescription: String? = null,
    imageAspectRatio: Float? = painter.intrinsicSize.width / painter.intrinsicSize.height.coerceAtLeast(
        1f
    ),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(shape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = rippleColor
                )
            )
            .then(
                if (imageAspectRatio != null && modifier.toString()
                        .contains("width") && !modifier.toString().contains("height")
                ) {
                    Modifier.aspectRatio(imageAspectRatio, matchHeightConstraintsFirst = false)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Display the image that represents the entire button
        Image(
            painter = painter,
            contentDescription = contentDescription,
            // Scale the image to fit the parent width
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewSdkImageButton() {
    SdkTheme {
        SdkImageButton(
            // Use an SVG drawable resource for the preview
            painter = painterResource(id = R.drawable.pay_with_coles_pay_button),
            contentDescription = "Pay with Coles Pay"
        ) {}
    }
}