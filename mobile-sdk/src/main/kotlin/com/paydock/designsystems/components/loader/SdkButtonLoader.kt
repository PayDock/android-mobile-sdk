package com.paydock.designsystems.components.loader

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.paydock.designsystems.theme.Theme

@PreviewLightDark
@Composable
internal fun SdkButtonLoader(
    loaderSize: Dp = Theme.dimensions.buttonLoaderSize,
    color: Color = Theme.colors.onPrimary,
    strokeWidth: Dp = Theme.dimensions.buttonLoaderWidth
) {
    CircularProgressIndicator(
        modifier = Modifier.size(loaderSize),
        color = color,
        strokeWidth = strokeWidth
    )
}