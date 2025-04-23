package com.paydock.designsystems.components.loader

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.paydock.designsystems.theme.BUTTON_LOADER_SIZE
import com.paydock.designsystems.theme.BUTTON_LOADER_WIDTH

@PreviewLightDark
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