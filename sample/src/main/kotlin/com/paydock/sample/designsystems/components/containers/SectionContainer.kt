package com.paydock.sample.designsystems.components.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun SectionContainer(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        TitleSection(modifier = Modifier.fillMaxWidth(), title = title, subTitle = subTitle)
        content()
    }
}

@LightDarkPreview
@Composable
private fun PreviewSectionContainer() {
    SampleTheme {
        SectionContainer(title = "Environment") {
            Text(text = "Random Content")
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewSectionContainerWithSubTitle() {
    SampleTheme {
        SectionContainer(title = "Environment", subTitle = "api-sandbox.paydock,com") {
            Text(text = "Random Content")
        }
    }
}