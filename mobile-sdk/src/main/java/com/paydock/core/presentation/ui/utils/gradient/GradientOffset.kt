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

package com.paydock.core.presentation.ui.utils.gradient

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
 * Creates a [GradientOffset] based on the specified [GradientAngle].
 *
 * @param angle The gradient angle.
 * @return The corresponding [GradientOffset].
 */
fun GradientOffset(angle: GradientAngle): GradientOffset {
    return when (angle) {
        GradientAngle.CW45 -> GradientOffset(
            start = Offset.Zero,
            end = Offset.Infinite
        )

        GradientAngle.CW90 -> GradientOffset(
            start = Offset.Zero,
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )

        GradientAngle.CW135 -> GradientOffset(
            start = Offset(Float.POSITIVE_INFINITY, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )

        GradientAngle.CW180 -> GradientOffset(
            start = Offset(Float.POSITIVE_INFINITY, 0f),
            end = Offset.Zero,
        )

        GradientAngle.CW225 -> GradientOffset(
            start = Offset.Infinite,
            end = Offset.Zero
        )

        GradientAngle.CW270 -> GradientOffset(
            start = Offset(0f, Float.POSITIVE_INFINITY),
            end = Offset.Zero
        )

        GradientAngle.CW315 -> GradientOffset(
            start = Offset(0f, Float.POSITIVE_INFINITY),
            end = Offset(Float.POSITIVE_INFINITY, 0f)
        )

        else -> GradientOffset(
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, 0f)
        )
    }
}

/**
 * Offset for [Brush.linearGradient] to rotate gradient depending on [start] and [end] offsets.
 */
data class GradientOffset(val start: Offset, val end: Offset)