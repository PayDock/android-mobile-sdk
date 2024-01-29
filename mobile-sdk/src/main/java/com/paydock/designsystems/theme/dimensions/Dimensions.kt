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
    val cornerRadius: Dp = MobileSDK.getInstance().sdkTheme.dimensions.cornerRadius,
    val spacing: Dp = MobileSDK.getInstance().sdkTheme.dimensions.spacing,
    val shadow: Dp = MobileSDK.getInstance().sdkTheme.dimensions.shadow,
    // SDK Internal Dimensions
    val buttonSpacing: Dp = 10.dp,
    val buttonIconSize: Dp = 20.dp,
    val buttonLoaderSize: Dp = 16.dp,
    val buttonHeight: Dp = 48.dp
)

internal fun dimensions(sdkTheme: MobileSDKTheme, isSmallDevice: Boolean = false) = Dimensions(
    borderWidth = if (isSmallDevice) sdkTheme.dimensions.borderWidth * 0.8f else sdkTheme.dimensions.borderWidth,
    cornerRadius = if (isSmallDevice) sdkTheme.dimensions.cornerRadius * 0.8f else sdkTheme.dimensions.cornerRadius,
    spacing = if (isSmallDevice) sdkTheme.dimensions.spacing * 0.8f else sdkTheme.dimensions.spacing,
    shadow = sdkTheme.dimensions.shadow
)