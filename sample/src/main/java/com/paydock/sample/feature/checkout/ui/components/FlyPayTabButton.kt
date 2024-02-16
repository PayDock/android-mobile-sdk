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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.designsystems.theme.typography.FontFamily

@Composable
fun FlyPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    val selectedColor = Theme.colors.primary
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = selectedColor,
        selectedBackgroundColor = Color.White,
        onClick = onClick
    ) {
        Text(
            text = stringResource(id = R.string.label_flypay),
            color = if (isSelected) Theme.colors.primary else Theme.colors.onSurface,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 16.8.sp,
                fontFamily = FontFamily,
                fontWeight = FontWeight(500)
            )
        )
    }
}

@Composable
@Preview
private fun PayPalTabButtonDefault() {
    SampleTheme {
        FlyPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun PayPalTabButtonSelected() {
    SampleTheme {
        FlyPayTabButton(
            isSelected = true
        ) {}
    }
}