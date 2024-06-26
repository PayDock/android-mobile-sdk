package com.paydock.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

/**
 * Custom holder for the Text styles instead of the one used in Material Design
 * Here You can put the styles the app needs and the one specified in you projects Figma
 *
 * secondary constructor uses Material Theme typography  from [Typography]
 * to maintain some corelation with Material Specification
 *
 * @property headline1 -
 * @property headline2 -
 * @property body1 - input text, component titles
 * @property body2 -
 * @property button - button text
 * @property label - error labels, input labels
 */
internal data class SdkTypography(
    val headline1: TextStyle,
    val headline2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val button: TextStyle,
    val label: TextStyle
) {

    constructor(typography: Typography) : this(
        headline1 = typography.titleLarge,
        headline2 = typography.titleMedium,
        body1 = typography.bodyMedium,
        body2 = typography.bodySmall,
        button = typography.bodyMedium,
        label = typography.labelMedium
    )
}
