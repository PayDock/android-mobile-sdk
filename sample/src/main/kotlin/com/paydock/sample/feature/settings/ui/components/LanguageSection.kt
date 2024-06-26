package com.paydock.sample.feature.settings.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.DropdownListField

@Preview
@Composable
fun LanguageSection() {
    // TODO - This will need to be updated with SDK Supported Languages (English by Default)
    SectionContainer(title = stringResource(R.string.label_language)) {
        val languages = listOf(stringResource(R.string.item_english))
        var selectedLanguage by remember { mutableStateOf(languages.first()) }
        DropdownListField(
            modifier = Modifier.fillMaxWidth(),
            items = languages,
            selected = selectedLanguage
        ) {
            selectedLanguage = it
        }
    }
}