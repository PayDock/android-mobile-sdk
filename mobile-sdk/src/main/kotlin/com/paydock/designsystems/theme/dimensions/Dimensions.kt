package com.paydock.designsystems.theme.dimensions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.MobileSDK
import com.paydock.ThemeDimensions

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

internal fun dimensions(dimensions: ThemeDimensions, isSmallDevice: Boolean = false) = Dimensions(
    borderWidth = if (isSmallDevice) dimensions.borderWidth * 0.8f else dimensions.borderWidth,
    textFieldCornerRadius = if (isSmallDevice) {
        dimensions.textFieldCornerRadius * 0.8f
    } else {
        dimensions.textFieldCornerRadius
    },
    buttonCornerRadius = if (isSmallDevice) {
        dimensions.buttonCornerRadius * 0.8f
    } else {
        dimensions.buttonCornerRadius
    },
    spacing = if (isSmallDevice) {
        dimensions.spacing * 0.8f
    } else {
        dimensions.spacing
    },
    shadow = dimensions.shadow
)