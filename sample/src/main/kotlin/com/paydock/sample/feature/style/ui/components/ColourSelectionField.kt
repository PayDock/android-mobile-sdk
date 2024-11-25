package com.paydock.sample.feature.style.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.core.extensions.toHexCode
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.feature.style.models.ColourTheme

@Composable
fun ColourSelectionField(
    modifier: Modifier = Modifier,
    colourTheme: ColourTheme,
    onItemClicked: (ColourTheme) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = colourTheme.themeName,
            style = Theme.typography.label,
            color = Theme.colors.onBackground
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = true) { onItemClicked(colourTheme) },
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .padding(14.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Text(
                    text = colourTheme.color.toHexCode(),
                    style = Theme.typography.label.copy(color = Theme.colors.onBackground)
                )
            }
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
                    .background(
                        color = colourTheme.color,
                        shape = RectangleShape
                    )
            )
        }
    }
}

@Preview
@Composable
private fun PreviewColorSelectionField() {
    SampleTheme() {
        PreviewColorSelectionField()
    }
}