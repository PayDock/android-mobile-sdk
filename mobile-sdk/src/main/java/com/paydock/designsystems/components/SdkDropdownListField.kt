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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.extensions.alpha20
import com.paydock.designsystems.theme.Theme
import kotlinx.coroutines.flow.filter

@Suppress("MagicNumber", "LongMethod")
@Composable
fun SdkDropdownListField(
    modifier: Modifier = Modifier,
    label: String,
    readOnly: Boolean = true,
    placeholder: String? = null,
    items: List<String>,
    selected: String? = items.firstOrNull(),
    onItemSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions
            .filter { it is PressInteraction.Press }
            .collect {
                expanded = !expanded
            }
    }
    DropdownListFieldStack(
        textField = {
            SdkTextField(
                modifier = modifier,
                value = selected ?: "",
                label = label,
                placeholder = placeholder ?: label,
                onValueChange = {

                },
                interactionSource = interactionSource,
                readOnly = readOnly,
                trailingIcon = {
                    val rotation by animateFloatAsState(
                        if (expanded) 180F else 0F,
                        label = "rotation"
                    )
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.ArrowDropDown),
                        contentDescription = stringResource(R.string.content_desc_dropdown_arrow),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            )
        },
        dropdownMenu = { boxWidth, itemHeight ->
            Box(
                Modifier
                    .width(boxWidth)
                    .wrapContentSize(Alignment.TopStart)
            ) {

                DropdownMenu(
                    modifier = Modifier.background(Theme.colors.primary.alpha20),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // This is to improve performance on large list
                    val menuHeight = itemHeight * items.count()
                    Box(
                        modifier = Modifier.size(
                            width = boxWidth,
                            height = if (menuHeight < 300.dp) menuHeight else 300.dp
                        )
                    ) {
                        LazyColumn {
                            items(items) { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = item,
                                            style = Theme.typography.body1,
                                            color = Theme.colors.onPrimary
                                        )
                                    },
                                    modifier = Modifier
                                        .height(itemHeight)
                                        .width(boxWidth)
                                        .background(if (item == selected) Theme.colors.primary else Color.Unspecified),
                                    onClick = {
                                        expanded = false
                                        onItemSelected(item)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
internal fun DropdownListFieldStack(
    textField: @Composable () -> Unit,
    dropdownMenu: @Composable (boxWidth: Dp, itemHeight: Dp) -> Unit
) {
    SubcomposeLayout { constraints ->
        val textFieldPlaceable =
            subcompose(ExposedDropdownMenuSlot.TextField, textField).first().measure(constraints)
        val dropdownPlaceable = subcompose(ExposedDropdownMenuSlot.Dropdown) {
            dropdownMenu(textFieldPlaceable.width.toDp(), textFieldPlaceable.height.toDp())
        }.first().measure(constraints)
        layout(textFieldPlaceable.width, textFieldPlaceable.height) {
            textFieldPlaceable.placeRelative(0, 0)
            dropdownPlaceable.placeRelative(0, textFieldPlaceable.height)
        }
    }
}

private enum class ExposedDropdownMenuSlot { TextField, Dropdown }