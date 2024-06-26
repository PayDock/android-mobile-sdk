package com.paydock.sample.feature.widgets.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.components.containers.TitleSection
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun WidgetRow(
    title: String,
    description: String,
    onClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClicked.invoke() }
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleSection(modifier = Modifier.weight(1f), title = title, subTitle = description)
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Chevron",
            modifier = Modifier.size(40.dp),
            tint = Theme.colors.onBackground
        )
    }
}

@Preview
@Composable
private fun PreviewWidgetRow() {
    SampleTheme {
        WidgetRow(title = "Card Details", description = "Tokenise card details") {

        }
    }
}