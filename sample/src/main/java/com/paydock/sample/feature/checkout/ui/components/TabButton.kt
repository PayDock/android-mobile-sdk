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

package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.Border

@Composable
fun TabButton(
    isSelected: Boolean,
    selectedBorderColor: Color,
    selectedBackgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .width(90.dp)
            .height(49.dp)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) selectedBorderColor else Border
                ),
                shape = RoundedCornerShape(size = 4.dp)
            )
            .background(
                color = if (isSelected) selectedBackgroundColor else Color.White,
                shape = RoundedCornerShape(size = 4.dp)
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}