package com.paydock.designsystems.theme.dimensions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.MobileSDK

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
 * @param spacing The spacing value used for margins and padding between UI elements.
 * @param textSpacing The vertical spacing between text elements. This is used for consistency between text areas. Defaults to 8.dp.
 * @param buttonSpacing The horizontal spacing between buttons when displayed in a group. Defaults to 8.dp.
 * @param buttonIconSize The size of icons displayed within buttons. Defaults to 20.dp.
 * @param buttonLoaderSize The size of the loading indicator (spinner) displayed within buttons. Defaults to 22.dp.
 * @param buttonLoaderWidth The stroke width of the loading indicator (spinner) displayed within buttons. Defaults to 2.dp.
 * @param buttonHeight The standard height of buttons. Defaults to 48.dp.
 * @param spacing A general-purpose spacing value used for margins and padding between UI elements. Defaults to 16.dp.
 */
@Immutable
internal class Dimensions(
    val borderWidth: Dp = MobileSDK.getInstance().sdkTheme.dimensions.borderWidth,
    val textFieldCornerRadius: Dp = MobileSDK.getInstance().sdkTheme.dimensions.textFieldCornerRadius,
    val buttonCornerRadius: Dp = MobileSDK.getInstance().sdkTheme.dimensions.buttonCornerRadius,
    val spacing: Dp = MobileSDK.getInstance().sdkTheme.dimensions.spacing,
    // SDK Internal Dimensions
    val textSpacing: Dp = 8.dp,
    val buttonSpacing: Dp = 8.dp,
    val buttonIconSize: Dp = 20.dp,
    val buttonLoaderSize: Dp = 22.dp,
    val buttonLoaderWidth: Dp = 2.dp,
    val buttonHeight: Dp = 48.dp,
)
