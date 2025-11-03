package com.paydock.sample.designsystems.components.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun SectionContainer(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        TitleSection(modifier = Modifier.fillMaxWidth(), title = title, subTitle = subTitle)
        content()
    }
}

@PreviewLightDark
@Composable
internal fun PreviewSectionContainer() {
    SampleTheme {
        SectionContainer(title = "Environment") {
            Text(text = "Random Content")
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewSectionContainerWithSubTitle() {
    SampleTheme {
        SectionContainer(title = "Environment", subTitle = "api-sandbox.paydock,com") {
            Text(text = "Random Content")
        }
    }
}