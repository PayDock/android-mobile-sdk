package com.paydock.designsystems.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import com.paydock.MobileSDK
import com.paydock.MobileSDKTheme
import com.paydock.ThemeColors
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.theme.dimensions.Dimensions
import com.paydock.designsystems.theme.dimensions.dimensions
import com.paydock.designsystems.theme.typography.SdkTypography
import com.paydock.designsystems.theme.typography.typography

/**
 * A composable function that provides theming for the Mobile SDK components.
 * This function sets up the MaterialTheme with colors, typography, and shapes
 * based on the provided `sdkTheme` and other parameters.
 *
 * @param sdkTheme The theme configuration for the Mobile SDK, defaulting to the SDK's theme.
 * @param isDarkMode Boolean indicating if dark mode should be used, defaulting to system setting.
 * @param isSmallDevice Boolean indicating if the device is considered small (e.g., a narrow screen).
 * @param content The composable content that will be wrapped with the SDK's theme.
 */
@Composable
internal fun SdkTheme(
    sdkTheme: MobileSDKTheme = MobileSDK.getInstance().sdkTheme,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    isSmallDevice: Boolean = isSmallDevice(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSdkDimensions provides dimensions(sdkTheme.dimensions, isSmallDevice),
    ) {
        MaterialTheme(
            colorScheme = if (isDarkMode) {
                darkColors(sdkTheme.getDarkColorTheme())
            } else {
                lightColors(
                    sdkTheme.getLightColorTheme()
                )
            },
            typography = typography(sdkTheme.font),
            shapes = buttonShapes(sdkTheme.dimensions)
        ) {
            content.invoke()
        }
    }
}

internal val LocalSdkDimensions = staticCompositionLocalOf {
    dimensions(MobileSDK.getInstance().sdkTheme.dimensions)
}

/**
 * Theme-related properties and objects for the Mobile SDK.
 */
internal object Theme {
    /**
     * Provides the current SDK typography.
     */
    val typography: SdkTypography @Composable get() = SdkTypography(MaterialTheme.typography)

    /**
     * Provides the current color scheme.
     */
    val colors: ColorScheme @Composable get() = MaterialTheme.colorScheme

    /**
     * Provides shapes for buttons based on the current theme.
     */
    val buttonShapes: Shapes @Composable get() = MaterialTheme.shapes

    /**
     * Provides shapes for text fields based on the current theme.
     */
    val textFieldShapes: Shapes @Composable get() = textFieldShapes(MobileSDK.getInstance().sdkTheme.dimensions)

    /**
     * Provides the current dimensions for the theme.
     */
    val dimensions: Dimensions @Composable get() = LocalSdkDimensions.current
}

/**
 * Generates a light color scheme for the Mobile SDK's theme.
 *
 * @param themeColor The color theme configuration used to derive light mode colors.
 * @return A [ColorScheme] configured for light mode.
 */
private fun lightColors(themeColor: ThemeColors.ThemeColor): ColorScheme = lightColorScheme(
    primary = themeColor.primary,
    onPrimary = themeColor.onPrimary,
    background = themeColor.background,
    onBackground = themeColor.placeholder,
    onSurface = themeColor.text,
    surface = themeColor.background,
    onSurfaceVariant = themeColor.placeholder,
    outline = themeColor.outline,
    error = themeColor.error
)

/**
 * Generates a dark color scheme for the Mobile SDK's theme.
 *
 * @param themeColor The color theme configuration used to derive dark mode colors.
 * @return A [ColorScheme] configured for dark mode.
 */
private fun darkColors(themeColor: ThemeColors.ThemeColor): ColorScheme = darkColorScheme(
    primary = themeColor.primary,
    onPrimary = themeColor.onPrimary,
    background = themeColor.background,
    onBackground = themeColor.placeholder,
    onSurface = themeColor.text,
    surface = themeColor.background,
    onSurfaceVariant = themeColor.placeholder,
    outline = themeColor.outline,
    error = themeColor.error
)

/**
 * Determines if the device is considered small based on its screen width.
 *
 * @return Boolean indicating if the device's screen width is less than or equal to the defined small screen size.
 */
@Composable
private fun isSmallDevice(): Boolean =
    LocalConfiguration.current.screenWidthDp <= MobileSDKConstants.General.SMALL_SCREEN_SIZE