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
import com.paydock.MobileSDK
import com.paydock.core.domain.model.Environment
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.DropdownListField

@Preview
@Composable
fun EnvironmentSection() {
    // TODO - This will need to be updated with SDK environment and base endpoint
    SectionContainer(
        title = stringResource(R.string.label_environment),
        subTitle = "api-sandbox.paydock,com"
    ) {
        val items = Environment.entries.toTypedArray().map { it.name }
        var selectedItem by remember { mutableStateOf(MobileSDK.getInstance().environment.name) }
        DropdownListField(
            modifier = Modifier.fillMaxWidth(),
            items = items,
            selected = selectedItem
        ) {
            selectedItem = it
        }
    }
}