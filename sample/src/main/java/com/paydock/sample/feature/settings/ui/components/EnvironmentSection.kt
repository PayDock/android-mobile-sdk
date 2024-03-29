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

package com.paydock.sample.feature.settings.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
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