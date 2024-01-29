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

package com.paydock.designsystems.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.extensions.alpha40
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.designsystems.type.AppButtonType

@Composable
internal fun SdkButton(
    modifier: Modifier = Modifier,
    text: String,
    iconVector: ImageVector? = null,
    @DrawableRes iconDrawable: Int? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonShape: Shape = Theme.shapes.small,
    type: AppButtonType = AppButtonType.Filled,
    onClick: () -> Unit,
) {
    when (type) {
        AppButtonType.Filled -> PrimaryButton(
            modifier = modifier.height(Theme.dimensions.buttonHeight),
            onClick = onClick,
            enabled = enabled && !isLoading,
            shape = buttonShape,
            content = { ButtonContent(text = text, vector = iconVector, drawableRes = iconDrawable, isLoading = isLoading) }
        )

        AppButtonType.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(Theme.dimensions.buttonHeight),
            enabled = enabled && !isLoading,
            content = { ButtonContent(text = text, isLoading = isLoading) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Theme.colors.primary,
                containerColor = Color.Transparent,
                disabledContainerColor = Theme.colors.primary.alpha40,
                disabledContentColor = Theme.colors.onPrimary
            ),
            shape = buttonShape,
            border = BorderStroke(1.dp, Theme.colors.primary)
        )

        AppButtonType.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !isLoading,
                shape = buttonShape,
                colors = ButtonDefaults.textButtonColors(
                    disabledContainerColor = Theme.colors.primary.alpha40,
                    disabledContentColor = Theme.colors.primary.alpha40,
                ),
                content = {
                    ButtonContent(text = text, isLoading = isLoading)
                }
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    shape: Shape,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Theme.colors.primary.alpha40,
            disabledContentColor = Theme.colors.onPrimary,
        ),
        shape = shape
    )
}

@Composable
private fun RowScope.ButtonContent(
    text: String,
    vector: ImageVector? = null,
    @DrawableRes drawableRes: Int? = null,
    isLoading: Boolean
) {
    Crossfade(targetState = isLoading, label = "buttonCrossFade") { loadingState ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loadingState) {
                CircularProgressIndicator(
                    color = Theme.colors.onPrimary,
                    modifier = Modifier.size(Theme.dimensions.buttonLoaderSize),
                    strokeWidth = 1.5.dp
                )
            } else {
                // Display the icon (if any) and text in a consistent layout
                if (vector != null) {
                    Icon(
                        modifier = Modifier.size(Theme.dimensions.buttonIconSize),
                        imageVector = vector,
                        contentDescription = stringResource(id = R.string.content_desc_button_icon),
                        tint = Theme.colors.onPrimary
                    )
                    Spacer(modifier = Modifier.width(Theme.dimensions.buttonSpacing))
                } else if (drawableRes != null) {
                    Icon(
                        modifier = Modifier.size(Theme.dimensions.buttonIconSize),
                        painter = painterResource(drawableRes),
                        contentDescription = stringResource(id = R.string.content_desc_button_icon),
                        tint = Theme.colors.onPrimary
                    )
                    Spacer(modifier = Modifier.width(Theme.dimensions.buttonSpacing))
                }
                Text(
                    text = text,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    style = Theme.typography.button.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Composable
@LightDarkPreview
private fun PreviewButtonPrimary() {
    SdkTheme {
        SdkButton(
            text = "Primary",
            onClick = {},
            type = AppButtonType.Filled,
        )
    }
}

@Composable
@LightDarkPreview
private fun PreviewOutlineButton() {
    SdkTheme {
        SdkButton(
            text = "Primary",
            onClick = {},
            type = AppButtonType.Outlined,
        )
    }
}

@Composable
@LightDarkPreview
private fun PreviewTextButton() {
    SdkTheme {
        SdkButton(
            text = "Primary",
            onClick = {},
            type = AppButtonType.Text,
        )
    }
}
