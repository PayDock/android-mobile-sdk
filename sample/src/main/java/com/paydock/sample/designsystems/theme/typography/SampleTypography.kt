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

package com.paydock.sample.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

/**
 * Custom holder for the Text styles instead of the one used in Material Design
 * Here You can put the styles the app needs and the one specified in you projects Figma
 *
 * secondary constructor uses Material Theme typography  from [Typography]
 * to maintain some corelation with Material Specification
 */
data class SampleTypography(
    val cardTitle: TextStyle,
    val navTitle: TextStyle,
    val body: TextStyle,
    val input: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,

//    val display1: TextStyle,
//    val display2: TextStyle,
//    val display3: TextStyle,
//    val display4: TextStyle,
//    val h1: TextStyle,
//    val h2: TextStyle,
//    val body1: TextStyle,
//    val body2: TextStyle,
//    val body3: TextStyle,
//    val labelLarge: TextStyle,
//    val labelMedium: TextStyle,
//    val labelSmall: TextStyle,
) {

    constructor(typography: Typography) : this(
        cardTitle = typography.titleLarge,
        navTitle = typography.titleMedium,
        body = typography.bodyMedium,
        input = typography.bodySmall,
        label = typography.labelMedium,
        caption = typography.labelSmall,
        button = typography.labelLarge
    )
}
