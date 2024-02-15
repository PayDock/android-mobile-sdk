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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.FlyPayBlue
import com.paydock.sample.designsystems.theme.PayPalYellow
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.designsystems.theme.typography.FontFamily

@Composable
fun FlyPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = FlyPayBlue,
        selectedBackgroundColor = FlyPayBlue,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_flypay),
            colorFilter = ColorFilter.tint(if (isSelected) Color.White else Theme.colors.onSurface),
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun FlyPayTabButtonDefault() {
    SampleTheme {
        FlyPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun FlyPayTabButtonSelected() {
    SampleTheme {
        FlyPayTabButton(
            isSelected = true
        ) {}
    }
}