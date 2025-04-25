package com.paydock.designsystems.components.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews

/**
 * Displays a progress indicator for SDK operations.
 *
 * This composable function creates a full-width box with a centered circular progress indicator.  The progress
 * of the indicator is controlled by a lambda expression that returns a float value representing the
 * current progress.
 *
 * @param modifier Modifier to be applied to the progress indicator.
 * @param progress A lambda expression that provides the current progress value as a float between 0.0 and 1.0.
 */
@Composable
internal fun SdkProgressLoader(modifier: Modifier = Modifier, progress: () -> Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = modifier, progress = progress)
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkProgressLoader() {
    SdkProgressLoader(progress = { 0.5f })
}