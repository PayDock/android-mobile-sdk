package com.paydock.designsystems.theme.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.paydock.MobileSDKTheme

@Suppress("LongMethod")
internal fun typography(sdkTheme: MobileSDKTheme) = Typography(
    displayLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp
    ),

    displayMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),

    displaySmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    titleLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    titleSmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),

    bodyLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Used for Input Text, Component Titles and Button Text
    bodyMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),

    // Used for Text Buttons (Links)
    bodySmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),

    labelLarge = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    // Used for Error and Input Labels
    labelMedium = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    labelSmall = TextStyle(
        fontFamily = sdkTheme.font.familyName,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
    )
)
