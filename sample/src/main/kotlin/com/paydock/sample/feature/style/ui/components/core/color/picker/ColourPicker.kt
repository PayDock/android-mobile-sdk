package com.paydock.sample.feature.style.ui.components.core.color.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
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
        val initialHsv = remember(color) { color.toHSV() }
        val hsv = remember {
            mutableStateOf(Triple(initialHsv[0], initialHsv[1], initialHsv[2]))
        }

        val alpha =
            remember(color) { mutableFloatStateOf(color.alpha) } // Initialize with incoming color's alpha

        // Calculate the result color from the updated hsv components
        val resultColor = remember(hsv.value, alpha.floatValue) { // Add alpha.value as a key
            mutableStateOf(
                // Use the updated Color constructor that takes alpha
                Color(
                    red = hsv.value.toColor().red, // Get RGB from HSV-derived color
                    green = hsv.value.toColor().green,
                    blue = hsv.value.toColor().blue,
                    alpha = alpha.floatValue // Apply the current alpha state
                )
            )
        }

        val (hexValue, setHexValue) = remember { mutableStateOf("") }
        LaunchedEffect(resultColor.value) { // Update hex based on the final resultColor (which includes alpha)
            setHexValue(resultColor.value.toHexCode())
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
                setHexValue(it)
            }
        )

        SatValPanel(defaultHSV = hsv.value) { sat, value ->
            hsv.value = Triple(hsv.value.first, sat, value)
        }

        HueBar(defaultHue = hsv.value.first) { hue ->
            hsv.value = Triple(hue, hsv.value.second, hsv.value.third)
        }

        AlphaSlider(
            currentAlpha = alpha.floatValue,
            onAlphaChanged = { newAlpha ->
                alpha.floatValue = newAlpha
            },
            // Pass the current HSV-derived color to show gradient against
            hsvBaseColor = hsv.value.toColor()
        )

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

@PreviewLightDark
@Composable
internal fun PreviewColourPicker() {
    SampleTheme {
        ColourPicker("#854646".color!!, {}, {})
    }
}