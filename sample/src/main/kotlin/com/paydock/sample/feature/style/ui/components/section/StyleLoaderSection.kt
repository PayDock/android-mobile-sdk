package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter
import com.paydock.sample.feature.style.ui.components.properties.loader.StrokeCapDropdown

@Composable
fun StyleLoaderSection(
    currentAppearance: LoaderAppearance,
    onAppearanceChange: (LoaderAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentLoaderColor = currentAppearance.color
    val currentTrackColor = currentAppearance.trackColor
    val currentStrokeWidth = currentAppearance.strokeWidth.value.toInt()
    val currentStrokeCap = currentAppearance.strokeCap

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorPickerField(
            label = stringResource(R.string.label_loader_colour),
            currentColor = currentLoaderColor,
            onColorChange = { newColor ->
                onAppearanceChange(
                    currentAppearance.copy(color = newColor)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        ColorPickerField(
            label = stringResource(R.string.label_loader_track_colour),
            currentColor = currentTrackColor,
            onColorChange = { newColor ->
                onAppearanceChange(
                    currentAppearance.copy(trackColor = newColor)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        NumberCounter(
            title = stringResource(R.string.label_loader_stroke_width),
            value = currentStrokeWidth,
            onValueChange = { newValue ->
                onAppearanceChange(
                    currentAppearance.copy(strokeWidth = newValue.dp)
                )
            }
        )

        StrokeCapDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentStrokeCap = currentStrokeCap,
            onStrokeCapChange = { newStrokeCap ->
                onAppearanceChange(currentAppearance.copy(strokeCap = newStrokeCap))
            },
        )
    }
}