package com.paydock.sample.designsystems.theme.typography

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSampleTypography = staticCompositionLocalOf {
    SampleTypography(Typography)
}

@Composable
fun ProvideSampleTypography(
    typography: SampleTypography = SampleTypography(Typography),
    content: @Composable () -> Unit = {},
) {
    CompositionLocalProvider(LocalSampleTypography provides typography) {
        content.invoke()
    }
}
