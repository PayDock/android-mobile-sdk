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
import androidx.compose.runtime.Immutable
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
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews

/**
 * Composable function for creating a custom image button.
 *
 * This button displays an image and handles click events.
 *
 * @param modifier The Modifier to be applied to the button.
 * @param enabled Whether the button is enabled or not. When disabled, the button's appearance changes
 *   (alpha is reduced based on `appearance.disabledImageAlpha`) and it does not respond to clicks.
 * @param appearance Defines the visual appearance of the button, including shape, ripple color, content scale, and disabled alpha.
 *                   See [ImageButtonDefaults.appearance] for default values.
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
    appearance: ImageButtonAppearance = ImageButtonDefaults.appearance(),
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
            .clip(appearance.shape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = appearance.rippleColor
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
                .alpha(if (enabled) 1f else appearance.disabledImageAlpha)
        )
    }
}

/**
 * Defines the visual appearance of an [SdkImageButton].
 * This class is immutable, ensuring that the appearance properties cannot be changed after creation,
 * which is beneficial for performance and predictability in Compose.
 *
 * @property shape The shape of the button's container.
 * @property rippleColor The color of the ripple effect when the button is pressed.
 * @property disabledImageAlpha The alpha value applied to the image when the button is disabled.
 *                              This value should be between 0.0f (fully transparent) and 1.0f (fully opaque).
 */
@Immutable
class ImageButtonAppearance(
    val shape: Shape,
    val rippleColor: Color,
    val disabledImageAlpha: Float
) {
    fun copy(
        shape: Shape = this.shape,
        rippleColor: Color = this.rippleColor,
        disabledImageAlpha: Float = this.disabledImageAlpha,
    ): ImageButtonAppearance = ImageButtonAppearance(
        shape = shape,
        rippleColor = rippleColor.copy(),
        disabledImageAlpha = disabledImageAlpha
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageButtonAppearance

        if (disabledImageAlpha != other.disabledImageAlpha) return false
        if (shape != other.shape) return false
        if (rippleColor != other.rippleColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = disabledImageAlpha.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + rippleColor.hashCode()
        return result
    }
}

/**
 * Object containing default values for [SdkIconButton].
 *
 * This object provides a default appearance for the image button, which can be used or customized.
 */
object ImageButtonDefaults {
    /**
     * Creates a default [ImageButtonAppearance] for an [SdkIconButton].
     *
     * This function provides a standard appearance configuration that can be used directly or as a
     * base for further customization. The default values are:
     * - `shape`: [ButtonDefaults.shape] (the default Material Design button shape)
     * - `rippleColor`: [Color.Transparent] (no visible ripple effect by default)
     * - `contentScale`: [ContentScale.FillWidth] (the image will scale to fill the width of the button)
     * - `disabledImageAlpha`: 0.5f (the image will be 50% transparent when the button is disabled)
     *
     * @return A default [ImageButtonAppearance] instance.
     */
    @Composable
    fun appearance(): ImageButtonAppearance =
        ImageButtonAppearance(
            shape = ButtonDefaults.shape,
            rippleColor = Color.Transparent,
            disabledImageAlpha = 0.5f
        )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkImageButton() {
    SdkImageButton(
        // Use an SVG drawable resource for the preview
        painter = painterResource(id = R.drawable.pay_with_coles_pay_button),
        contentDescription = "Pay with Coles Pay"
    ) {}
}