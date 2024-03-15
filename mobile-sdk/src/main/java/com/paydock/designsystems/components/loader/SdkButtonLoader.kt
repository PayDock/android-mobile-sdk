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

package com.paydock.designsystems.components.loader

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.Theme

@LightDarkPreview
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