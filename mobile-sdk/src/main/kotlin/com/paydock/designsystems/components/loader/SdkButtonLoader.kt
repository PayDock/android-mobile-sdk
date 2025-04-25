package com.paydock.designsystems.components.loader

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.theme.BUTTON_LOADER_SIZE
import com.paydock.designsystems.theme.BUTTON_LOADER_WIDTH

/**
 *  A composable function that displays a circular loading indicator, typically used within a button to
 *  indicate ongoing background processing, like a network request.
 *
 *  @param loaderSize The diameter of the circular progress indicator. Defaults to [BUTTON_LOADER_SIZE].
 *  @param color The color of the progress indicator. Defaults to [MaterialTheme.colorScheme.onPrimary],
 *               suitable for use on a primary-colored button.
 *  @param strokeWidth The width of the circular progress indicator's stroke. Defaults to [BUTTON_LOADER_WIDTH].
 */
@SdkLightDarkPreviews
@Composable
internal fun SdkButtonLoader(
    loaderSize: Dp = BUTTON_LOADER_SIZE,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    strokeWidth: Dp = BUTTON_LOADER_WIDTH
) {
    CircularProgressIndicator(
        modifier = Modifier.size(loaderSize),
        color = color,
        strokeWidth = strokeWidth
    )
}