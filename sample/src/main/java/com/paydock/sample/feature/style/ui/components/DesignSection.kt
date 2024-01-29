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

package com.paydock.sample.feature.style.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.ThemeDimensions
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.NumberCounter

@Composable
fun DesignSection(
    dimensionsTheme: ThemeDimensions,
    onDimensionsUpdated: (ThemeDimensions) -> Unit
) {
    SectionContainer(title = stringResource(R.string.label_design)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_corner_radius),
                value = dimensionsTheme.cornerRadius.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(cornerRadius = it.dp))
                })
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_shadow),
                value = dimensionsTheme.shadow.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(shadow = it.dp))
                })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_border_width),
                value = dimensionsTheme.borderWidth.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(borderWidth = it.dp))
                })
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_spacing),
                value = dimensionsTheme.spacing.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(spacing = it.dp))
                })
        }
    }
}