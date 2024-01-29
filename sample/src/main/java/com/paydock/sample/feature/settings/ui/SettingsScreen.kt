/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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