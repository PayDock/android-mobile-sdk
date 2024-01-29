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

package com.paydock.sample.core.extensions

import android.os.Build
import androidx.compose.ui.graphics.Color

val String.color
    get() = try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        null
    }

fun Color.toHexCode(): String {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format("#%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
}

fun Color.toHexCodeWithAlpha(): String {
    val alpha = this.alpha * 255
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format(
        "#%02x%02x%02x%02x",
        alpha.toInt(),
        red.toInt(),
        green.toInt(),
        blue.toInt()
    )
}

// Extension function to convert hsv to Color
fun Triple<Float, Float, Float>.toColor(): Color {
    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(first, second, third))
    return Color(colorInt)
}

fun Color.toHSV(): FloatArray {
    val hsv = FloatArray(3)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        android.graphics.Color.colorToHSV(android.graphics.Color.argb(alpha, red, green, blue), hsv)
    } else {
        android.graphics.Color.RGBToHSV(
            (this.red * 255).toInt(),
            (this.green * 255).toInt(),
            (this.blue * 255).toInt(),
            hsv
        )
    }
    return hsv
}