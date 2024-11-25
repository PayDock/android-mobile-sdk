package com.paydock.designsystems.theme.dimensions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.MobileSDK
import com.paydock.MobileSDKTheme

/**
 * Holder for the app dimensions that can be used to avoid hardcoded margins and paddings
 */
@Immutable
internal class Dimensions(
    val borderWidth: Dp = MobileSDK.getInstance().sdkTheme.dimensions.borderWidth,
    val textFieldCornerRadius: Dp = MobileSDK.getInstance().sdkTheme.dimensions.textFieldCornerRadius,
    val buttonCornerRadius: Dp = MobileSDK.getInstance().sdkTheme.dimensions.buttonCornerRadius,
    val spacing: Dp = MobileSDK.getInstance().sdkTheme.dimensions.spacing,
    val shadow: Dp = MobileSDK.getInstance().sdkTheme.dimensions.shadow,
    // SDK Internal Dimensions
    val textSpacing: Dp = 8.dp,
    val buttonSpacing: Dp = 8.dp,
    val buttonIconSize: Dp = 20.dp,
    val buttonLoaderSize: Dp = 22.dp,
    val buttonLoaderWidth: Dp = 2.dp,
    val buttonHeight: Dp = 48.dp,
)

internal fun dimensions(sdkTheme: MobileSDKTheme, isSmallDevice: Boolean = false) = Dimensions(
    borderWidth = if (isSmallDevice) sdkTheme.dimensions.borderWidth * 0.8f else sdkTheme.dimensions.borderWidth,
    textFieldCornerRadius = if (isSmallDevice) {
        sdkTheme.dimensions.textFieldCornerRadius * 0.8f
    } else
        sdkTheme.dimensions.textFieldCornerRadius,
    buttonCornerRadius = if (isSmallDevice) {
        sdkTheme.dimensions.buttonCornerRadius * 0.8f
    } else
        sdkTheme.dimensions.buttonCornerRadius,
    spacing = if (isSmallDevice) {
        sdkTheme.dimensions.spacing * 0.8f
    } else
        sdkTheme.dimensions.spacing,
    shadow = sdkTheme.dimensions.shadow
)