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
