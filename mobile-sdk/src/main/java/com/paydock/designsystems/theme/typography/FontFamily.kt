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

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.paydock.R

internal val AcidGroteskNormal = Font(
    resId = R.font.acid_grotesk,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
)

internal val AcidGroteskFontFamily = FontFamily(
    AcidGroteskNormal
)

internal val AcidGroteskFontList = listOf(AcidGroteskNormal)

internal val ArialNormal = Font(
    resId = R.font.arial,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
)

internal val ArialFontFamily = FontFamily(
    ArialNormal
)

internal val ArialFontList = listOf(ArialNormal)