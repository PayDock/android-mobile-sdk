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

package com.paydock.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.paydock.MobileSDKTheme

@Suppress("LongMethod")
internal fun typography(sdkTheme: MobileSDKTheme) = Typography(
    displayLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp
    ),

    displayMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),

    displaySmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    titleLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    titleSmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),

    bodyLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Used for Input Text, Component Titles and Button Text
    bodyMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),

    // Used for Text Buttons (Links)
    bodySmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),

    labelLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    // Used for Error and Input Labels
    labelMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    labelSmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
    )
)
