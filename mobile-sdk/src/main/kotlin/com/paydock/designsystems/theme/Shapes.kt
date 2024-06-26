package com.paydock.designsystems.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.paydock.MobileSDKTheme

@Suppress("MagicNumber")
internal fun shapes(sdkTheme: MobileSDKTheme) = Shapes(
    // Uses default corner radius applied
    small = RoundedCornerShape(sdkTheme.dimensions.cornerRadius),
    medium = RoundedCornerShape(sdkTheme.dimensions.cornerRadius * 2),
    large = RoundedCornerShape(sdkTheme.dimensions.cornerRadius * 7)
)
