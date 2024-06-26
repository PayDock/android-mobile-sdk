package com.paydock.designsystems.theme.typography

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.paydock.R

internal val AcidGroteskNormal = Font(
    resId = R.font.acid_grotesk,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
)

internal val AcidGroteskFontFamily = FontFamily(
    AcidGroteskNormal
)

internal val AcidGroteskFontList = listOf(AcidGroteskNormal)

internal val ArialNormal = Font(
    resId = R.font.arial,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
)

internal val ArialFontFamily = FontFamily(
    ArialNormal
)

internal val ArialFontList = listOf(ArialNormal)