/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

package com.paydock.sample.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    //    displayLarge = TextStyle(
    //        fontSize = 34.sp
    //    ),
    //
    //    displayMedium = TextStyle(
    //        fontSize = 28.sp,
    //        lineHeight = 32.sp
    //    ),
    //
    //    displaySmall = TextStyle(
    //        fontSize = 30.sp
    //    ),
    //
    //    headlineLarge = TextStyle(
    //        fontSize = 30.sp
    //    ),
    //
    //    headlineMedium = TextStyle(
    //        fontSize = 24.sp
    //    ),
    //
    //    headlineSmall = TextStyle(
    //        fontSize = 20.sp
    //    ),

    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.4.sp,
        fontFamily = FontFamily
    ),

    titleMedium = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontFamily = FontFamily
    ),

    //    titleSmall = TextStyle(
    //        fontSize = 17.sp,
    //        lineHeight = 22.sp
    //    ),

    //    bodyLarge = TextStyle(
    //        fontSize = 18.sp,
    //        lineHeight = 24.sp
    //    ),

    bodyMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.32.sp,
        fontFamily = FontFamily
    ),

    bodySmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily
    ),

    // Used for buttons
    labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFamily = FontFamily,
        fontWeight = FontWeight(400)
    ),

    labelMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily
    ),

    labelSmall = TextStyle(
        fontSize = 10.sp,
        fontFamily = FontFamily
    )
)
