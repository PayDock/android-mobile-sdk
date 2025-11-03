package com.paydock.sample.feature.style.ui.components.core.color.picker

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.core.extensions.color
import com.paydock.sample.core.extensions.toHSV
import com.paydock.sample.core.extensions.toHexCode
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ColourPicker(
    initialColor: Color, // Renamed for clarity, this is the starting point
    onColourUpdated: (Color) -> Unit,
    onCanceled: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        // Main state holders for Hue, Saturation, Value, and Alpha
        // These are reset if `initialColor` changes.
        var hsv by remember(initialColor) {
            val hsvArray = initialColor.toHSV() // Assuming Color.toHSV() returns FloatArray(3)
            mutableStateOf(Triple(hsvArray[0], hsvArray[1], hsvArray[2]))
        }
        var alpha by remember(initialColor) { mutableFloatStateOf(initialColor.alpha) }

        // Derived color based on the current HSV and Alpha state
        val currentColor = remember(hsv, alpha) {
            Color.hsv(hsv.first, hsv.second, hsv.third, alpha)
        }

        // State for the Hex input field
        var hexInput by remember { mutableStateOf("") }

        // Effect to update hexInput when currentColor changes (e.g., from sliders)
        // This also sets the initial hex value.
        LaunchedEffect(currentColor) {
            hexInput =
                currentColor.toHexCode(includeAlpha = false) // Or true, if your hex field supports it
        }

        ColorInputField(
            color = hexInput, // Driven by hexInput state
            readOnly = false, // Or based on some other logic if needed
            onResultColorChanged = { colorFromHexInput ->
                // When hex input results in a valid color
                val newHsvArray = colorFromHexInput.toHSV()
                hsv = Triple(newHsvArray[0], newHsvArray[1], newHsvArray[2])
                // Optionally, if hex input can define alpha (e.g., #AARRGGBB):
                // alpha = colorFromHexInput.alpha
                // If hex input does NOT define alpha, don't change the current alpha here.
            },
            onTextColorChanged = { currentTextInput ->
                hexInput = currentTextInput // Update hexInput as user types
            }
        )

        SatValPanel(
            currentHSV = hsv, // Pass the full HSV triple
            setSatVal = { saturation, value ->
                hsv = Triple(hsv.first, saturation, value)
            }
        )

        HueBar(
            currentHue = hsv.first,
            onHueChanged = { newHue ->
                Log.d("ColourPicker", "Hue changed to: $newHue. Current hsv: $hsv")
                hsv = Triple(newHue, hsv.second, hsv.third) // Creates a NEW Triple instance
                Log.d("ColourPicker", "NEW hsv: $hsv")
            }
        )

        AlphaSlider(
            currentAlpha = alpha,
            // The base color for the alpha slider's gradient (fully opaque version of current color)
            hsvBaseColor = Color.hsv(hsv.first, hsv.second, hsv.third, 1f),
            onAlphaChanged = { newAlpha ->
                alpha = newAlpha
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Assuming AppButton is a standard Composable Button
            Button( // Using Material3 Button as a placeholder
                modifier = Modifier.weight(1f),
                onClick = onCanceled
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button( // Using Material3 Button as a placeholder
                modifier = Modifier.weight(1f),
                onClick = {
                    onColourUpdated(currentColor) // Pass the final derived color
                }
            ) {
                Text("OK")
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