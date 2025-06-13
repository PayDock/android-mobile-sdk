package com.paydock.sample.feature.style.ui.components.core.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.core.extensions.toHexCode
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.style.models.ColourTheme

@Composable
fun ColourSelectionField(
    modifier: Modifier = Modifier,
    rawColourTheme: ColourTheme,
    onItemClicked: () -> Unit,
) {
    // Resolve Unspecified to a concrete color for display purposes here
    val displayColor = if (rawColourTheme.color == Color.Unspecified) {
        LocalContentColor.current
    } else {
        rawColourTheme.color
    }
    val colourThemeForDisplay = rawColourTheme.copy(color = displayColor)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = colourThemeForDisplay.themeName,
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = true) { onItemClicked() },
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(14.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Text(text = colourThemeForDisplay.color.toHexCode())
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .background(
                        color = colourThemeForDisplay.color,
                        shape = RectangleShape
                    )
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewColorSelectionField() {
    SampleTheme() {
        ColourSelectionField(rawColourTheme = ColourTheme("Test", Color.Red), onItemClicked = {})
    }
}