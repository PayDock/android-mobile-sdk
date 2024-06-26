package com.paydock.designsystems.theme.typography

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.paydock.MobileSDK

internal val LocalSdkTypography = staticCompositionLocalOf {
    SdkTypography(typography(MobileSDK.getInstance().sdkTheme))
}

@Composable
internal fun ProvideSdkTypography(
    typography: SdkTypography = SdkTypography(typography(MobileSDK.getInstance().sdkTheme)),
    content: @Composable () -> Unit = {},
) {
    CompositionLocalProvider(LocalSdkTypography provides typography) {
        content.invoke()
    }
}
