package com.paydock.sample.designsystems.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.dimensions.Dimensions
import com.paydock.sample.designsystems.theme.dimensions.LocalDimensions
import com.paydock.sample.designsystems.theme.dimensions.ProvideDimensions
import com.paydock.sample.designsystems.theme.typography.LocalSampleTypography
import com.paydock.sample.designsystems.theme.typography.ProvideSampleTypography
import com.paydock.sample.designsystems.theme.typography.SampleTypography
import com.paydock.sample.designsystems.theme.typography.Typography

/**
 * Main theme provider
 * Use [Theme.*] to access colors, typography and etc
 */
@Composable
fun SampleTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    deviceSize: DeviceSize = getDeviceSize(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkMode -> DarkColors
        else -> LightColors
    }
    val dimensions = when (deviceSize) {
        DeviceSize.SMALL -> Dimensions.Small
        DeviceSize.MEDIUM,
        DeviceSize.LARGE -> Dimensions.Default
    }
    ProvideDimensions(dimensions = dimensions) {
        ProvideSampleTypography {
            MaterialTheme(
                typography = Typography,
                shapes = Shapes,
                content = content,
                colorScheme = colorScheme
            )
        }
    }
}

/**
 * Shortcut to obtain App Theme values instead of using [MaterialTheme]
 * - Provides custom AppTypography instead of Material one
 * - Provides access to dimensions, that can vary based on device size
 */
object Theme {
    val typography: SampleTypography @Composable get() = LocalSampleTypography.current
    val colors: ColorScheme @Composable get() = MaterialTheme.colorScheme
    val shapes: Shapes @Composable get() = MaterialTheme.shapes
    val dimensions: Dimensions @Composable get() = LocalDimensions.current
}

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = PaydockBlack,
    primaryContainer = Color.White,
    onPrimaryContainer = PaydockBlack,

    secondary = Taupe,
    onSecondary = PaydockBlack,
    secondaryContainer = Color.White,
    onSecondaryContainer = PaydockBlack,

    tertiary = Pink,
    onTertiary = PaydockBlack,
    tertiaryContainer = Color.White,
    onTertiaryContainer = PaydockBlack,

    background = OffWhite,
    onBackground = PaydockBlack,

    surface = Color.White,
    onSurface = PaydockBlack,
    surfaceVariant = OffWhiteLight,
    surfaceTint = OffWhiteDark,

    error = Error,
    onError = PaydockWhite,
    errorContainer = Color.White,
    onErrorContainer = Error,

    outline = PaydockBlack,
    outlineVariant = PaydockBlackLight,

    scrim = OffWhite
)

private val DarkColors = darkColorScheme(
    primary = TealDark,
    onPrimary = PaydockBlack,
    primaryContainer = PaydockBlack,
    onPrimaryContainer = Color.White,

    secondary = TaupeDark,
    onSecondary = PaydockBlack,
    secondaryContainer = PaydockBlack,
    onSecondaryContainer = Color.White,

    tertiary = PinkDark,
    onTertiary = PaydockBlack,
    tertiaryContainer = PaydockBlack,
    onTertiaryContainer = Color.White,

    background = PaydockBlack,
    onBackground = PaydockWhite,

    surface = PaydockBlack,
    onSurface = Color.White,
    surfaceVariant = PaydockWhiteDark,
    surfaceTint = OffWhiteDark,

    error = Error,
    onError = Color.White,
    errorContainer = PaydockBlack,
    onErrorContainer = Error,

    outline = PaydockBlack,
    outlineVariant = PaydockBlackLight,

    scrim = PaydockBlack
)

@Composable
internal fun getDeviceSize(): DeviceSize {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    return when {
        screenWidthDp <= 300.dp -> DeviceSize.SMALL
        screenWidthDp <= 600.dp -> DeviceSize.MEDIUM
        else -> DeviceSize.LARGE
    }
}