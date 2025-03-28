package com.paydock.designsystems.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.paydock.ThemeDimensions

/**
 * Defines shapes for text fields based on the provided Mobile SDK theme.
 *
 * @param dimensions The theme dimension values for text field corner radii.
 * @return A [Shapes] object with defined small, medium, and large corner radii for text fields.
 *
 * @Suppress("MagicNumber") is used to ignore hardcoded multipliers applied to corner radius values.
 */
@Suppress("MagicNumber")
internal fun textFieldShapes(dimensions: ThemeDimensions) = Shapes(
    // Applies a default corner radius for text fields based on theme dimensions.
    small = RoundedCornerShape(dimensions.textFieldCornerRadius),
    medium = RoundedCornerShape(dimensions.textFieldCornerRadius * 2),
    large = RoundedCornerShape(dimensions.textFieldCornerRadius * 7)
)

/**
 * Defines shapes for buttons based on the provided Mobile SDK theme.
 *
 * @param dimensions The theme dimension values for button corner radii.
 * @return A [Shapes] object with defined small, medium, and large corner radii for buttons.
 *
 * @Suppress("MagicNumber") is used to ignore hardcoded multipliers applied to corner radius values.
 */
@Suppress("MagicNumber")
internal fun buttonShapes(dimensions: ThemeDimensions) = Shapes(
    // Applies a default corner radius for buttons based on theme dimensions.
    small = RoundedCornerShape(dimensions.buttonCornerRadius),
    medium = RoundedCornerShape(dimensions.buttonCornerRadius * 2),
    large = RoundedCornerShape(dimensions.buttonCornerRadius * 7)
)
