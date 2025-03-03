package com.paydock.designsystems.theme.dimensions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.ThemeDimensions
import com.paydock.designsystems.theme.DeviceSize

/**
 * Holder for the app dimensions that can be used to avoid hardcoded margins and paddings.
 *
 * This class provides a centralized location for defining and accessing various dimensions
 * used throughout the application's UI.  By using these pre-defined values, you can maintain
 * consistency in the visual appearance and make it easier to update the design across the app.
 *
 * @param borderWidth The width of borders used for UI elements like text fields and containers.
 *                    Defaults to the `borderWidth` defined in the `MobileSDK`'s theme.
 * @param textFieldCornerRadius The corner radius applied to text fields, giving them rounded corners.
 *                              Defaults to the `textFieldCornerRadius` defined in the `MobileSDK`'s theme.
 * @param buttonCornerRadius The corner radius applied to buttons, giving them rounded corners.
 *                           Defaults to the `buttonCornerRadius` defined in the `MobileSDK`'s theme.
 * @param textSpacing The vertical spacing between text elements. This is used for consistency between text areas. Defaults to 8.dp.
 * @param buttonSpacing The horizontal spacing between buttons when displayed in a group. Defaults to 8.dp.
 * @param buttonIconSize The size of icons displayed within buttons. Defaults to 20.dp.
 * @param buttonLoaderSize The size of the loading indicator (spinner) displayed within buttons. Defaults to 22.dp.
 * @param buttonLoaderWidth The stroke width of the loading indicator (spinner) displayed within buttons. Defaults to 2.dp.
 * @param buttonHeight The standard height of buttons. Defaults to 48.dp.
 * @param spacing A general-purpose spacing value used for margins and padding between UI elements. Defaults to 16.dp.
 */
@Immutable
internal data class Dimensions(
    val borderWidth: Dp,
    val textFieldCornerRadius: Dp,
    val buttonCornerRadius: Dp,
    // SDK Internal Dimensions
    val textSpacing: Dp,
    val buttonSpacing: Dp,
    val buttonIconSize: Dp,
    val buttonLoaderSize: Dp,
    val buttonLoaderWidth: Dp,
    val buttonHeight: Dp,
    val spacing: Dp,
) {
    companion object {
        // Default values for internal dimensions
        internal val DefaultTextSpacing: Dp = 8.dp
        internal val DefaultButtonSpacing: Dp = 8.dp
        internal val DefaultButtonIconSize: Dp = 20.dp
        internal val DefaultButtonLoaderSize: Dp = 22.dp
        internal val DefaultButtonLoaderWidth: Dp = 2.dp
        internal val DefaultButtonHeight: Dp = 48.dp
        internal val DefaultSpacing: Dp = 16.dp

        // Factory method to create Dimensions with default internal values
        fun createDefault(
            borderWidth: Dp,
            textFieldCornerRadius: Dp,
            buttonCornerRadius: Dp
        ): Dimensions = Dimensions(
            borderWidth = borderWidth,
            textFieldCornerRadius = textFieldCornerRadius,
            buttonCornerRadius = buttonCornerRadius,
            textSpacing = DefaultTextSpacing,
            buttonSpacing = DefaultButtonSpacing,
            buttonIconSize = DefaultButtonIconSize,
            buttonLoaderSize = DefaultButtonLoaderSize,
            buttonLoaderWidth = DefaultButtonLoaderWidth,
            buttonHeight = DefaultButtonHeight,
            spacing = DefaultSpacing
        )
    }
}

/**
 * Calculates the final dimensions based on the provided theme dimensions and device size.
 *
 * This function takes a set of [ThemeDimensions] and a [DeviceSize] as input.
 * It then scales the theme dimensions according to the device size to create a
 * [Dimensions] object suitable for the current screen.
 *
 * The scaling is applied as follows:
 * - [DeviceSize.SMALL]: 80% of the original theme dimensions.
 * - [DeviceSize.MEDIUM]: 90% of the original theme dimensions.
 * - [DeviceSize.LARGE]: 100% of the original theme dimensions (no scaling).
 *
 * @param themeDimensions The base [ThemeDimensions] to scale.
 * @param deviceSize The [DeviceSize] representing the current device's screen size.
 * @return A [Dimensions] object with scaled values for borderWidth, textFieldCornerRadius, and buttonCornerRadius.
 */
@Suppress("MagicNumber")
internal fun dimensions(themeDimensions: ThemeDimensions, deviceSize: DeviceSize): Dimensions {
    val scaleFactor = when (deviceSize) {
        DeviceSize.SMALL -> 0.8f
        DeviceSize.MEDIUM -> 0.9f
        DeviceSize.LARGE -> 1.0f
    }

    return Dimensions.createDefault(
        borderWidth = themeDimensions.borderWidth * scaleFactor,
        textFieldCornerRadius = themeDimensions.textFieldCornerRadius * scaleFactor,
        buttonCornerRadius = themeDimensions.buttonCornerRadius * scaleFactor,
    ).copy(
        textSpacing = Dimensions.DefaultTextSpacing * scaleFactor,
        buttonSpacing = Dimensions.DefaultButtonSpacing * scaleFactor,
        buttonIconSize = Dimensions.DefaultButtonIconSize * scaleFactor,
        buttonLoaderSize = Dimensions.DefaultButtonLoaderSize * scaleFactor,
        buttonLoaderWidth = Dimensions.DefaultButtonLoaderWidth * scaleFactor,
        buttonHeight = Dimensions.DefaultButtonHeight * scaleFactor,
        spacing = Dimensions.DefaultSpacing * scaleFactor,
    )
}