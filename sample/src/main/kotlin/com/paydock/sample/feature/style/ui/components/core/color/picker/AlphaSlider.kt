package com.paydock.sample.feature.style.ui.components.core.color.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.style.ui.components.core.color.CheckerboardBackground

@Composable
fun AlphaSlider(
    currentAlpha: Float,
    onAlphaChanged: (Float) -> Unit,
    hsvBaseColor: Color, // The opaque color derived from current HSV selection
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.width(300.dp)) {
        Text(
            text = "Alpha: ${(currentAlpha * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // Create a gradient from transparent version of hsvBaseColor to opaque hsvBaseColor
        val transparentBase = hsvBaseColor.copy(alpha = 0f)
        val opaqueBase = hsvBaseColor.copy(alpha = 1f) // Should already be opaque

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(vertical = 8.dp) // Add some padding around the slider gradient
        ) {
            // Background checkerboard for the slider itself
            CheckerboardBackground(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(50))
            )

            // Alpha gradient bar
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(transparentBase, opaqueBase)
                        ),
                        shape = RoundedCornerShape(50) // Assuming you want rounded corners like HueBar
                    )
                    .clip(RoundedCornerShape(50)) // Clip for the slider interaction
            )
            Slider(
                value = currentAlpha,
                onValueChange = onAlphaChanged,
                modifier = Modifier.matchParentSize(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, // Or a color that contrasts well
                    activeTrackColor = Color.Transparent, // Gradient is the track
                    inactiveTrackColor = Color.Transparent // Gradient is the track
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewAlphaSlider() {
    val color = Color.Blue
    val alpha = color.alpha
    SampleTheme {
        AlphaSlider(currentAlpha = alpha, hsvBaseColor = color, onAlphaChanged = {})
    }
}