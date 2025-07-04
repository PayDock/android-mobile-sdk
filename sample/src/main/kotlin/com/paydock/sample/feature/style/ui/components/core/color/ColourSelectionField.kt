package com.paydock.sample.feature.style.ui.components.core.color

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.isUnspecified
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
    val displayColor = if (rawColourTheme.color.isUnspecified) { // << BREAKPOINT HERE
        LocalContentColor.current
    } else {
        rawColourTheme.color
    }
    // It's good practice to ensure displayColor itself is not Unspecified for drawing
    val colorToDraw = if (displayColor.isUnspecified) {
        // Decide a concrete color if LocalContentColor itself could be Unspecified
        // or if you want a specific "undefined" visual.
        // For now, assuming LocalContentColor.current is always concrete.
        LocalContentColor.current
    } else {
        displayColor
    }
    val colourThemeForDisplay = rawColourTheme.copy(color = colorToDraw)

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
                // Consider if toHexCode should represent ARGB (e.g., #AARRGGBB)
                Text(text = colourThemeForDisplay.color.toHexCode())
            }

            // Enhanced Color Swatch Box
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                // Layer 1: Checkerboard background (only visible if colorToDraw is transparent)
                CheckerboardBackground(Modifier.matchParentSize())

                // Layer 2: The actual color
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = colorToDraw, // Use the resolved colorToDraw
                            shape = RectangleShape
                        )
                )
            }
        }
    }
}

@Composable
fun CheckerboardBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val squareSize = size.minDimension / 2f // Adjust for finer/coarser checkerboard
        val lightColor = Color.LightGray.copy(alpha = 0.4f) // Softer checkerboard
        val darkColor = Color.DarkGray.copy(alpha = 0.4f)

        clipRect { // Clip drawing to the bounds of the Canvas
            var isLight = true
            for (y in 0 until (size.height / squareSize).toInt() + 1) {
                for (x in 0 until (size.width / squareSize).toInt() + 1) {
                    drawRect(
                        color = if (isLight) lightColor else darkColor,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x * squareSize,
                            y * squareSize
                        ),
                        size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                    )
                    isLight = !isLight
                }
                // Ensure row starts with alternating color if width isn't perfectly divisible
                if ((size.width / squareSize).toInt() % 2 == 0) {
                    isLight = !isLight
                }
            }
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