/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

package com.paydock.sample.designsystems.components.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
internal fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    PrimaryButton(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        enabled = enabled && !isLoading,
        content = { ButtonContent(text = text, isLoading = isLoading) }
    )
}

@Composable
private fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = ButtonDefaults.buttonColors(
        disabledContainerColor = Theme.colors.primary.copy(alpha = 0.4f),
        disabledContentColor = Theme.colors.onSurface
    )
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
        colors = colors,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(vertical = Theme.dimensions.medium2)
    )
}

@Composable
private fun RowScope.ButtonContent(text: String, isLoading: Boolean) {
    Crossfade(targetState = isLoading, label = "coss_fade") {
        if (it) {
            CircularProgressIndicator(
                color = Theme.colors.onSurface,
                modifier = Modifier.size(24.dp),
                strokeWidth = 1.dp
            )
        } else {
            Text(
                text = text,
                style = Theme.typography.button
            )
        }
    }
}

@Composable
@LightDarkPreview
private fun PreviewButtonPrimary() {
    SampleTheme {
        AppButton(
            text = "Primary",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@LightDarkPreview
private fun PreviewButtonPrimaryWithLoading() {
    SampleTheme() {
        AppButton(
            text = "Primary",
            onClick = {},
            isLoading = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
