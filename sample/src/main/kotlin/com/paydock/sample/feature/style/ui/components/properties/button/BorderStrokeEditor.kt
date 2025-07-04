package com.paydock.sample.feature.style.ui.components.properties.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter

@Composable
fun BorderStrokeEditor(
    modifier: Modifier = Modifier,
    currentBorderStroke: BorderStroke?,
    onBorderStrokeChange: (BorderStroke?) -> Unit,
    // Default values to use when a border is first enabled
    defaultEnabledBorderStroke: BorderStroke = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.primary
    )
) {
    var editableWidth by remember { mutableStateOf(defaultEnabledBorderStroke.width) }
    var editableColor by remember { mutableStateOf(Color.Unspecified) } // Default to unspecified

    // Effect to update editable states when currentBorderStroke changes from outside
    // or when isBorderEnabled changes.
    LaunchedEffect(currentBorderStroke) {
        // Optionally reset to defaults or keep last values in a disabled UI
        editableWidth = defaultEnabledBorderStroke.width
        val defaultBrush = defaultEnabledBorderStroke.brush
        editableColor = if (defaultBrush is SolidColor) defaultBrush.value else Color.Black
    }

    SectionContainer(title = stringResource(R.string.label_border_stroke), modifier = modifier) {
        NumberCounter(
            title = stringResource(R.string.label_border_width),
            value = editableWidth.value.toInt(),
            onValueChange = { newWidthDp ->
                val newWidth = newWidthDp.dp
                editableWidth = newWidth
                onBorderStrokeChange(
                    BorderStroke(
                        newWidth,
                        SolidColor(editableColor)
                    )
                )
            }
        )

        ColorPickerField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_checked_border_colour),
            currentColor = editableColor,
            onColorChange = { newColor ->
                onBorderStrokeChange(
                    BorderStroke(
                        editableWidth,
                        SolidColor(newColor)
                    )
                )
            }
        )
    }
}