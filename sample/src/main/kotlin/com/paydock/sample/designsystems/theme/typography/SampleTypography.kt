package com.paydock.sample.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.sp

/**
 * Custom holder for the Text styles instead of the one used in Material Design
 * Here You can put the styles the app needs and the one specified in you projects Figma
 *
 * secondary constructor uses Material Theme typography  from [Typography]
 * to maintain some corelation with Material Specification
 */
data class SampleTypography(
    val sectionHeader: TextStyle,
    val cardTitle: TextStyle,
    val cardDescription: TextStyle,
    val navTitle: TextStyle,
    val body: TextStyle,
    val input: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,

//    val display1: TextStyle,
//    val display2: TextStyle,
//    val display3: TextStyle,
//    val display4: TextStyle,
//    val h1: TextStyle,
//    val h2: TextStyle,
//    val body1: TextStyle,
//    val body2: TextStyle,
//    val body3: TextStyle,
//    val labelLarge: TextStyle,
//    val labelMedium: TextStyle,
//    val labelSmall: TextStyle,
) {

    constructor(typography: Typography) : this(
        sectionHeader = TextStyle(
            fontWeight = SemiBold,
            fontSize = 18.sp,
            lineHeight = 25.sp,
            fontFamily = FontFamily
        ),
        cardTitle = typography.titleLarge,
        cardDescription = TextStyle(
            fontWeight = Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily
        ),
        navTitle = typography.titleMedium,
        body = typography.bodyMedium,
        input = typography.bodySmall,
        label = typography.labelMedium,
        caption = typography.labelSmall,
        button = typography.labelLarge
    )
}
