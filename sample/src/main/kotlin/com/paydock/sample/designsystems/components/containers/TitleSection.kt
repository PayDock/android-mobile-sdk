package com.paydock.sample.designsystems.components.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

@Composable
fun TitleSection(modifier: Modifier = Modifier, title: String, subTitle: String? = null) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        SectionTitle(modifier = Modifier.fillMaxWidth(), title = title)
        subTitle?.let { SubSectionTitle(it) }
    }
}

@Composable
fun SectionTitle(modifier: Modifier = Modifier, title: String) {
    Text(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun SubSectionTitle(subTitle: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = subTitle,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@PreviewLightDark
@Composable
private fun PreviewTitleSectionWithSubtitle() {
    TitleSection(title = "Main Title", subTitle = "This is a subtitle")
}

@PreviewLightDark
@Composable
private fun PreviewTitleSectionWithoutSubtitle() {
    TitleSection(title = "Main Title Only", subTitle = null)
}
