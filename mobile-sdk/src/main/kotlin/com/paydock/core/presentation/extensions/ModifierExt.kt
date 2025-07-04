package com.paydock.core.presentation.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Extension function for applying a gradient background to a composable.
 *
 * @param colors The list of colors to use for the gradient.
 * @param angle The angle (in degrees) of the gradient.
 * @return A new modifier with the specified gradient background.
 */
@Suppress("MagicNumber")
internal fun Modifier.gradientBackground(colors: List<Color>, angle: Float) = this.then(
    Modifier.drawBehind {
        // Convert angle from degrees to radians
        val angleRad = angle / 180f * PI

        // Calculate the fractional x and y components based on the angle
        val x = cos(angleRad).toFloat()
        val y = sin(angleRad).toFloat()

        // Calculate the radius of the gradient
        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f

        // Calculate the offset for the center based on the angle and radius
        val offset = center + Offset(x * radius, y * radius)

        // Ensure that the offset is within the bounds of the size
        val exactOffset = Offset(
            x = min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
        )

        // Draw the gradient rectangle using linear gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
            size = size
        )
    }
)

/**
 * Extension function to calculate the scaled size based on the font scale.
 *
 * @param baseSize The base size (in Dp) before scaling.
 * @param fontScale The font scale factor from LocalConfiguration.
 * @return The scaled size as Dp.
 */
internal fun Dp.scaled(fontScale: Float): Dp {
    return (this * (1 + (fontScale - 1) * 0.5f))
}

/**
 * Extension function to calculate the scaled size based on the font scale.
 *
 * @param baseSize The base size (in Float) before scaling.
 * @param fontScale The font scale factor from LocalConfiguration.
 * @return The scaled size as Float.
 */
internal fun Float.scaled(fontScale: Float): Float {
    return this * (1 + (fontScale - 1) * 0.5f)
}

/**
 * Applies error semantics to a Modifier if the `isError` flag is true.
 * This is useful for accessibility, allowing screen readers to announce errors.
 *
 * @param isError A boolean flag indicating whether there is an error.
 * @param defaultErrorMessage The default message to be announced if there is an error.
 * @return The original Modifier if `isError` is false, or a new Modifier with error semantics if `isError` is true.
 */
internal fun Modifier.defaultErrorSemantics(
    isError: Boolean,
    defaultErrorMessage: String,
): Modifier = if (isError) semantics { error(defaultErrorMessage) } else this

/**
 * A custom Modifier that dynamically adjusts bottom padding so the Composable
 * stays above the on-screen keyboard (IME), even when default insets fail.
 *
 * Combines dynamic position-based padding with [Modifier.imePadding] for robustness.
 */
fun Modifier.positionAwareImePadding(): Modifier = composed {
    // Tracks the amount of padding (in pixels) to apply at the bottom
    var consumePadding by remember { mutableIntStateOf(0) }

    this
        // Gets the current position and size of the Composable on the screen
        .onGloballyPositioned { coordinates ->
            val root = coordinates.findRootCoordinates()

            // Calculates the bottom position of the Composable in the window
            val bottom = coordinates.positionInWindow().y + coordinates.size.height

            // Computes how much space is between the Composable's bottom and the screen's bottom
            // This value is then used as padding to keep it above the keyboard
            consumePadding = (root.size.height - bottom).toInt().coerceAtLeast(0)
        }
        // Consumes the calculated padding (converted to dp) to shift the Composable upward
        .consumeWindowInsets(PaddingValues(bottom = consumePadding.dp))
        // Adds Compose’s built-in imePadding as a fallback for any remaining adjustments
        .imePadding()
}