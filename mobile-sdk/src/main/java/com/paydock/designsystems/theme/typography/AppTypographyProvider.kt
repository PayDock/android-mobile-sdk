/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
