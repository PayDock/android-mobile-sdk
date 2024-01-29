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

package com.paydock.sample.core.presentation.ui.layout

import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import com.paydock.core.presentation.ui.preview.LightDarkPreview

@Composable
internal fun ColumnWithSeparators(
    modifier: Modifier = Modifier,
    separator: @Composable () -> Unit = { Divider() },
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val contentsMeasurebles = subcompose("content", content)
        val separators = contentsMeasurebles.map { subcompose("separator$it", separator).first() }
        val separatorsPlaceables = separators.map { it.measure(constraints) }
        val contentPlaceables = contentsMeasurebles.map { it.measure(constraints) }

        val totalHeight =
            listOf(separatorsPlaceables, contentPlaceables).flatten().sumOf { it.height }

        layout(constraints.maxWidth, totalHeight) {
            val dividerHeight = separatorsPlaceables.maxOfOrNull { it.height } ?: 0
            var y = 0

            val itemHeights = contentPlaceables.mapIndexed { index, placeable ->
                if (index != 0) y += dividerHeight

                placeable.place(x = 0, y = y)
                y += placeable.height
                ContentChild(y)
            }

            separatorsPlaceables.forEachIndexed { index, placeable ->
                if (index != contentPlaceables.size - 1) {
                    placeable.place(x = 0, y = itemHeights[index].height)
                }
            }
        }
    }
}

private data class ContentChild(val height: Int)

@LightDarkPreview
@Composable
private fun PreviewColumnWithSeparators() {
    ColumnWithSeparators {
        repeat(times = 10) { Text(text = "Hello $it") }
    }
}
