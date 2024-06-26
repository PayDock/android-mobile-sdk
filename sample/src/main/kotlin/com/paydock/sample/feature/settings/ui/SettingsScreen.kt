package com.paydock.sample.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.core.presentation.ui.layout.ColumnWithSeparators
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.settings.ui.components.EnvironmentSection
import com.paydock.sample.feature.settings.ui.components.LanguageSection
import com.paydock.sample.feature.settings.ui.components.SecretKeySection

@Composable
fun SettingsScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp)
    ) {
        ColumnWithSeparators(content = {
            EnvironmentSection()
            SecretKeySection()
            LanguageSection()
        })
    }
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    SampleTheme {
        SettingsScreen()
    }
}