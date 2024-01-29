/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.paydock.core.SMALL_SCREEN_SIZE
import com.paydock.designsystems.theme.dimensions.Dimensions
import com.paydock.designsystems.theme.dimensions.LocalSdkDimensions
import com.paydock.designsystems.theme.dimensions.ProvideDimensions
import com.paydock.designsystems.theme.dimensions.dimensions
import com.paydock.designsystems.theme.typography.LocalSdkTypography
import com.paydock.designsystems.theme.typography.ProvideSdkTypography
import com.paydock.designsystems.theme.typography.SdkTypography
import com.paydock.designsystems.theme.typography.typography

/**
 * Main theme provider
 * Use [Theme.*] to access colors, typography and etc
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
                shapes = shapes(sdkTheme),
                colorScheme = if (isDarkMode) darkColors(sdkTheme) else lightColors(sdkTheme),
                content = content
            )
        }
    }
}

internal object Theme {
    val typography: SdkTypography @Composable get() = LocalSdkTypography.current
    val colors: ColorScheme @Composable get() = MaterialTheme.colorScheme
    val shapes: Shapes @Composable get() = MaterialTheme.shapes
    val dimensions: Dimensions @Composable get() = LocalSdkDimensions.current
}

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

@Composable
private fun isSmallDevice(): Boolean = LocalConfiguration.current.screenWidthDp <= SMALL_SCREEN_SIZE
