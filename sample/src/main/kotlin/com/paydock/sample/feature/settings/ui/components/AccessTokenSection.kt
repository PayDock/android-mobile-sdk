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
import androidx.hilt.navigation.compose.hiltViewModel
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.feature.settings.SettingsViewModel

@Preview
@Composable
fun AccessTokenSection(viewModel: SettingsViewModel = hiltViewModel()) {
    var accessToken by remember { mutableStateOf(viewModel.accessToken.value) }
    SectionContainer(title = stringResource(R.string.label_access_token)) {
        SettingsInputField(
            modifier = Modifier.fillMaxWidth(),
            value = accessToken,
            onValueChange = {
                accessToken = it
                viewModel.updateAccessToken(it)
            })
    }
}
