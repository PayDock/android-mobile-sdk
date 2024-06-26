package com.paydock.sample.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    //    displayLarge = TextStyle(
    //        fontSize = 34.sp
    //    ),
    //
    //    displayMedium = TextStyle(
    //        fontSize = 28.sp,
    //        lineHeight = 32.sp
    //    ),
    //
    //    displaySmall = TextStyle(
    //        fontSize = 30.sp
    //    ),
    //
    //    headlineLarge = TextStyle(
    //        fontSize = 30.sp
    //    ),
    //
    //    headlineMedium = TextStyle(
    //        fontSize = 24.sp
    //    ),
    //
    //    headlineSmall = TextStyle(
    //        fontSize = 20.sp
    //    ),

    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.4.sp,
        fontFamily = FontFamily
    ),

    titleMedium = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontFamily = FontFamily
    ),

    //    titleSmall = TextStyle(
    //        fontSize = 17.sp,
    //        lineHeight = 22.sp
    //    ),

    //    bodyLarge = TextStyle(
    //        fontSize = 18.sp,
    //        lineHeight = 24.sp
    //    ),

    bodyMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.32.sp,
        fontFamily = FontFamily
    ),

    bodySmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily
    ),

    // Used for buttons
    labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFamily = FontFamily,
        fontWeight = FontWeight(400)
    ),

    labelMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily
    ),

    labelSmall = TextStyle(
        fontSize = 10.sp,
        fontFamily = FontFamily
    )
)
