package com.paydock.designsystems.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.paydock.MobileSDK
import com.paydock.MobileSDKTheme
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.theme.dimensions.Dimensions
import com.paydock.designsystems.theme.dimensions.LocalSdkDimensions
import com.paydock.designsystems.theme.dimensions.ProvideDimensions
import com.paydock.designsystems.theme.dimensions.dimensions
import com.paydock.designsystems.theme.typography.LocalSdkTypography
import com.paydock.designsystems.theme.typography.ProvideSdkTypography
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
    ProvideDimensions(dimensions = dimensions(sdkTheme, isSmallDevice)) {
        ProvideSdkTypography {
            MaterialTheme(
                typography = typography(sdkTheme),
                shapes = buttonShapes(sdkTheme),
                colorScheme = if (isDarkMode) darkColors(sdkTheme) else lightColors(sdkTheme),
                content = content
            )
        }
    }
}

/**
 * Theme-related properties and objects for the Mobile SDK.
 */
internal object Theme {
    /**
     * Provides the current SDK typography.
     */
    val typography: SdkTypography @Composable get() = LocalSdkTypography.current

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
    val textFieldShapes: Shapes @Composable get() = textFieldShapes(MobileSDK.getInstance().sdkTheme)

    /**
     * Provides the current dimensions for the theme.
     */
    val dimensions: Dimensions @Composable get() = LocalSdkDimensions.current
}

/**
 * Generates a light color scheme for the Mobile SDK's theme.
 *
 * @param sdkTheme The theme configuration used to derive light mode colors.
 * @return A [ColorScheme] configured for light mode.
 */
private fun lightColors(sdkTheme: MobileSDKTheme): ColorScheme = lightColorScheme(
    primary = sdkTheme.getLightColorTheme().primary,
    onPrimary = sdkTheme.getLightColorTheme().onPrimary,
    background = sdkTheme.getLightColorTheme().background,
    onBackground = sdkTheme.getLightColorTheme().placeholder,
    onSurface = sdkTheme.getLightColorTheme().text,
    surface = sdkTheme.getLightColorTheme().background,
    onSurfaceVariant = sdkTheme.getLightColorTheme().placeholder,
    outline = sdkTheme.getLightColorTheme().outline,
    error = sdkTheme.getLightColorTheme().error
)

/**
 * Generates a dark color scheme for the Mobile SDK's theme.
 *
 * @param sdkTheme The theme configuration used to derive dark mode colors.
 * @return A [ColorScheme] configured for dark mode.
 */
private fun darkColors(sdkTheme: MobileSDKTheme): ColorScheme = darkColorScheme(
    primary = sdkTheme.getDarkColorTheme().primary,
    onPrimary = sdkTheme.getDarkColorTheme().onPrimary,
    background = sdkTheme.getDarkColorTheme().background,
    onBackground = sdkTheme.getDarkColorTheme().placeholder,
    onSurface = sdkTheme.getDarkColorTheme().text,
    surface = sdkTheme.getDarkColorTheme().background,
    onSurfaceVariant = sdkTheme.getDarkColorTheme().placeholder,
    outline = sdkTheme.getDarkColorTheme().outline,
    error = sdkTheme.getDarkColorTheme().error
)

/**
 * Determines if the device is considered small based on its screen width.
 *
 * @return Boolean indicating if the device's screen width is less than or equal to the defined small screen size.
 */
@Composable
private fun isSmallDevice(): Boolean =
    LocalConfiguration.current.screenWidthDp <= MobileSDKConstants.General.SMALL_SCREEN_SIZE
