/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
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

package com.paydock.core.presentation.ui.extensions

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Extension property for [TextStyle] that returns a new [TextStyle] instance with bold font weight.
 */
internal val TextStyle.bold: TextStyle
    get() = copy(fontWeight = FontWeight.Bold)

/**
 * Extension property for [TextStyle] that returns a new [TextStyle] instance with normal font weight.
 */
internal val TextStyle.normal: TextStyle
    get() = copy(fontWeight = FontWeight.Normal)

/**
 * Extension property for [TextStyle] that returns a new [TextStyle] instance with light font weight.
 */
internal val TextStyle.light: TextStyle
    get() = copy(fontWeight = FontWeight.Light)
