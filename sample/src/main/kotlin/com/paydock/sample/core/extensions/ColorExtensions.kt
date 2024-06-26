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