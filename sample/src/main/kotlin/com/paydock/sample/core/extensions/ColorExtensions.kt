package com.paydock.sample.core.extensions

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

val String.color
    get() = try {
        Color(this.toColorInt())
    } catch (e: Exception) {
        null
    }

fun Color.toHexCode(includeAlpha: Boolean = true): String {
    if (this == Color.Unspecified) {
        return "Unspecified"
    }
    val red = (this.red * 255).toInt().coerceIn(0, 255)
    val green = (this.green * 255).toInt().coerceIn(0, 255)
    val blue = (this.blue * 255).toInt().coerceIn(0, 255)
    return if (includeAlpha) {
        val alpha = (this.alpha * 255).toInt().coerceIn(0, 255)
        String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    } else {
        String.format("#%02X%02X%02X", red, green, blue)
    }
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