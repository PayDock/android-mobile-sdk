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

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Extension function for applying a gradient background to a composable.
 *
 * @param colors The list of colors to use for the gradient.
 * @param angle The angle (in degrees) of the gradient.
 * @return A new modifier with the specified gradient background.
 */
@Suppress("MagicNumber")
fun Modifier.gradientBackground(colors: List<Color>, angle: Float) = this.then(
    Modifier.drawBehind {
        // Convert angle from degrees to radians
        val angleRad = angle / 180f * PI

        // Calculate the fractional x and y components based on the angle
        val x = cos(angleRad).toFloat()
        val y = sin(angleRad).toFloat()

        // Calculate the radius of the gradient
        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f

        // Calculate the offset for the center based on the angle and radius
        val offset = center + Offset(x * radius, y * radius)

        // Ensure that the offset is within the bounds of the size
        val exactOffset = Offset(
            x = min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
        )

        // Draw the gradient rectangle using linear gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
            size = size
        )
    }
)