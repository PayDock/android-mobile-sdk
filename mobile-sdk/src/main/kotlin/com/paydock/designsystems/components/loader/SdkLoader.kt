package com.paydock.designsystems.components.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.paydock.core.presentation.ui.preview.LightDarkPreview

@LightDarkPreview
@Composable
internal fun SdkLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}