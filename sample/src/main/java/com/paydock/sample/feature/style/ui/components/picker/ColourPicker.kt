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

package com.paydock.sample.feature.style.ui.components.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.core.extensions.color
import com.paydock.sample.core.extensions.toColor
import com.paydock.sample.core.extensions.toHSV
import com.paydock.sample.core.extensions.toHexCode
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ColourPicker(color: Color, onColourUpdated: (Color) -> Unit, onCanceled: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        // HSV (Hue, Saturation, Value)
        val hsv = remember {
            val hsv = color.toHSV()
            mutableStateOf(
                Triple(hsv[0], hsv[1], hsv[2])
            )
        }

        // Calculate the result color from the updated hsv components
        val resultColor = remember(hsv.value) {
            mutableStateOf(hsv.value.toColor())
        }

        val (hexValue, setValue) = remember { mutableStateOf(color.toHexCode()) }

        LaunchedEffect(hsv.value) {
            setValue(hsv.value.toColor().toHexCode())
        }

        ColorInputField(
            color = hexValue,
            readOnly = false,
            onResultColorChanged = {
                val tempHSV = floatArrayOf(0f, 0f, 0f)
                android.graphics.Color.colorToHSV(it.toArgb(), tempHSV)
                hsv.value = Triple(tempHSV[0], tempHSV[1], tempHSV[2])
            },
            onTextColorChanged = {
                setValue(it)
            }
        )

        SatValPanel(defaultHSV = hsv.value) { sat, value ->
            hsv.value = Triple(hsv.value.first, sat, value)
        }

        HueBar(defaultHue = hsv.value.first) { hue ->
            hsv.value = Triple(hue, hsv.value.second, hsv.value.third)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppButton(
                modifier = Modifier.weight(1f),
                text = "Cancel"
            ) {
                onCanceled()
            }

            Spacer(modifier = Modifier.width(16.dp))

            AppButton(
                modifier = Modifier.weight(1f),
                text = "OK"
            ) {
                onColourUpdated(resultColor.value)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewStyleScreen() {
    SampleTheme {
        ColourPicker("#854646".color!!, {}, {})
    }
}