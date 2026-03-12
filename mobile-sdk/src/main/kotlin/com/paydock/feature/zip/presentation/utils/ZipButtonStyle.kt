package com.paydock.feature.zip.presentation.utils

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.R

/**
 * Zip button styles following Zip's official brand guidelines.
 *
 * @see <a href="https://www.zippartner.co/">Zip Brand Guidelines</a>
 */
enum class ZipButtonStyle {
    /**
     * White/colored icon on filled black background (recommended default).
     */
    WHITE_ON_BLACK,

    /**
     * Black/colored icon on white background with black border.
     */
    BLACK_ON_WHITE;

    /**
     * Get the background color for this style.
     */
    val backgroundColor: Color
        get() = when (this) {
            WHITE_ON_BLACK -> Color(0xFF1A0826) // Zip brand dark purple/black
            BLACK_ON_WHITE -> Color(0xFFFFFFFa) // Off-white
        }

    /**
     * Get the border color for this style.
     */
    val borderColor: Color
        get() = when (this) {
            WHITE_ON_BLACK -> Color.Transparent
            BLACK_ON_WHITE -> Color.Black
        }

    /**
     * Get the border width for this style.
     */
    val borderWidth: Dp
        get() = when (this) {
            WHITE_ON_BLACK -> 0.dp
            BLACK_ON_WHITE -> 1.dp
        }

    /**
     * Get the drawable resource ID for this style.
     */
    @get:DrawableRes
    val imageResId: Int
        get() = when (this) {
            WHITE_ON_BLACK -> R.drawable.ic_zip_logo_white
            BLACK_ON_WHITE -> R.drawable.ic_zip_logo_colored
        }

    /**
     * Get the loader/spinner color appropriate for this button style.
     */
    val loaderColor: Color
        get() = when (this) {
            WHITE_ON_BLACK -> Color(0xFFFFFFFa) // White spinner on dark background
            BLACK_ON_WHITE -> Color(0xFF1A0826) // Dark spinner on white background
        }
}