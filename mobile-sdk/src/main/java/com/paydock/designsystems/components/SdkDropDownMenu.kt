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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.paydock.core.presentation.ui.extensions.alpha20
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

@Composable
internal fun <String : Any> SdkDropDownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    itemWidth: Dp,
    items: List<String>,
    dismissOnClickOutside: Boolean = true,
    isClickEnabled: Boolean = true,
    onItemSelected: (String) -> Unit,
    onDismissed: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(itemWidth)
            .wrapContentSize(Alignment.TopStart)
    ) {
        DropdownMenu(
            modifier = modifier.background(Theme.colors.primary.alpha20).testTag("sdkDropDownMenu"),
            properties = PopupProperties(
                dismissOnClickOutside = dismissOnClickOutside
            ),
            expanded = expanded,
            onDismissRequest = {
                onDismissed()
            },
        ) {
            items.forEach { item: String ->
                DropdownMenuItem(
                    modifier = Modifier.width(itemWidth),
                    text = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = AnnotatedString(text = item.toString()),
                            style = Theme.typography.body1,
                            color = Theme.colors.onSurface
                        )
                    },
                    enabled = isClickEnabled,
                    onClick = {
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewSdkDropDownMenuExpanded() {
    SdkTheme {
        SdkDropDownMenu(
            expanded = true,
            itemWidth = 300.dp,
            items = listOf("Item 1", "Item 2", "Item 3"),
            onItemSelected = {},
            onDismissed = {}
        )
    }
}