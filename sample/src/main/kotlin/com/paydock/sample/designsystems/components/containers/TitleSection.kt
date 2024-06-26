package com.paydock.sample.designsystems.components.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun TitleSection(modifier: Modifier = Modifier, title: String, subTitle: String?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        SectionTitle(title)
        subTitle?.let { SubSectionTitle(it) }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = title,
        style = Theme.typography.cardTitle,
        color = Theme.colors.onBackground
    )
}

@Composable
fun SubSectionTitle(subTitle: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = subTitle,
        style = Theme.typography.body,
        color = Color(0xFF8C8C8C),
        fontWeight = FontWeight(400)
    )
}