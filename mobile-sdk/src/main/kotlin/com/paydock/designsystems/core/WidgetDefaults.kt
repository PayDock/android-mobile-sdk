package com.paydock.designsystems.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default values and constants used across various widgets.
 */
object WidgetDefaults {
    val Spacing = 16.dp

    /**
     * Determines whether a widget's fields should use a column (vertical) layout
     * instead of a row (horizontal) layout based on available space and font scaling.
     *
     * This calculation considers both the actual screen width and the current font scale
     * to compute an "effective width" - the usable space accounting for enlarged text.
     * It then compares this against the minimum required width calculated from the
     * field requirements.
     *
     * **Formula:**
     * - Effective Width = Available Width / Font Scale
     * - Min Required Width = Min Field Width / Field Weight
     * - Use Column Layout if: Effective Width < Min Required Width
     *
     * **Example Usage:**
     * ```kotlin
     * BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
     *     val shouldUseColumn = WidgetDefaults.shouldUseColumnLayout(
     *         availableWidth = maxWidth,
     *         fontScale = fontScale,
     *         minFieldWidth = 120.dp,  // Minimum width for the smallest field
     *         fieldWeight = 0.3f       // Weight that field occupies in row layout
     *     )
     *     if (shouldUseColumn) { /* Column layout */ }
     *     else { /* Row layout */ }
     * }
     * ```
     *
     * @param availableWidth The total width available for the layout (typically from BoxWithConstraints.maxWidth)
     * @param fontScale The current system font scale multiplier (from LocalConfiguration.current.fontScale)
     * @param minFieldWidth The minimum comfortable width required for the smallest field in the layout
     * @param fieldWeight The proportional weight of the smallest field when using row layout (e.g., 0.3f for 30%)
     * @return `true` if fields should be stacked vertically in a column, `false` if they can fit side-by-side in a row
     */
    fun shouldUseColumnLayout(
        availableWidth: Dp,
        fontScale: Float,
        minFieldWidth: Dp,
        fieldWeight: Float
    ): Boolean {
        // Calculate effective width accounting for font scaling
        // As font scale increases, effective usable space decreases
        val effectiveWidth = availableWidth / fontScale

        // Calculate minimum total width needed for row layout
        // Based on the smallest field's requirements and its weight allocation
        val minTotalEffectiveWidth = minFieldWidth / fieldWeight

        // Switch to column if we don't have enough effective space
        return effectiveWidth < minTotalEffectiveWidth
    }
}