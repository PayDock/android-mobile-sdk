package com.paydock.designsystems.theme.dimensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.paydock.MobileSDK

internal val LocalSdkDimensions = staticCompositionLocalOf {
    dimensions(MobileSDK.getInstance().sdkTheme)
}

@Composable
internal fun ProvideDimensions(
    dimensions: Dimensions = Dimensions(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSdkDimensions provides dimensions) {
        content.invoke()
    }
}
