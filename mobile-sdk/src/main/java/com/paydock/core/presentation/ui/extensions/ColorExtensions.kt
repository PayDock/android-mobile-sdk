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

import androidx.compose.ui.graphics.Color

/**
 * Extension function for the [Color] companion object that creates a Color instance
 * from a hexadecimal color representation.
 *
 * @param hex The hexadecimal color representation (e.g., "#RRGGBB" or "#AARRGGBB").
 * @return The Color instance created from the hexadecimal representation.
 */
internal fun Color.Companion.fromHex(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}

/**
 * Property extension for the [Color] class that returns a new Color instance
 * with an alpha value of 0.2.
 */
internal val Color.alpha20 get() = copy(alpha = 0.2f)

/**
 * Property extension for the [Color] class that returns a new Color instance
 * with an alpha value of 0.4.
 */
internal val Color.alpha40 get() = copy(alpha = 0.4f)
